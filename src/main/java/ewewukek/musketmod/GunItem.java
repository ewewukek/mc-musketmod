package ewewukek.musketmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public abstract class GunItem extends Item {
    public static final int LOADING_STAGE_1 = 5;
    public static final int LOADING_STAGE_2 = 10;
    public static final int LOADING_STAGE_3 = 20;
    public static final int RELOAD_DURATION = 30;

    // for RenderHelper
    public static ItemStack activeMainHandStack;
    public static ItemStack activeOffhandStack;

    public GunItem(Properties properties) {
        super(properties);
    }

    public abstract float bulletStdDev();
    public abstract float bulletSpeed();
    public abstract float damageMultiplierMin();
    public abstract float damageMultiplierMax();
    public abstract SoundEvent fireSound();
    public abstract boolean twoHanded();
    public abstract boolean ignoreInvulnerableTime();

    public boolean canUseFrom(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return true;
        }
        if (twoHanded()) {
            return false;
        }
        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!mainHandStack.isEmpty() && mainHandStack.getItem() instanceof GunItem) {
            return !((GunItem)mainHandStack.getItem()).twoHanded();
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand hand) {
        if (!canUseFrom(player, hand)) return super.use(worldIn, player, hand);

        ItemStack stack = player.getItemInHand(hand);
        boolean creative = player.getAbilities().instabuild;

        if (player.isEyeInFluid(FluidTags.WATER) && !creative) {
            return InteractionResultHolder.fail(stack);
        }

        // shoot from left hand if both are loaded
        if (hand == InteractionHand.MAIN_HAND && !twoHanded() && isLoaded(stack)) {
            ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof GunItem) {
                GunItem offhandGun = (GunItem)offhandStack.getItem();
                if (!offhandGun.twoHanded() && isLoaded(offhandStack)) {
                    return InteractionResultHolder.pass(stack);
                }
            }
        }

        boolean haveAmmo = !findAmmo(player).isEmpty() || creative;
        boolean loaded = isLoaded(stack);

        if (loaded) {
            if (!worldIn.isClientSide) {
                Vec3 front = Vec3.directionFromRotation(player.getXRot(), player.getYRot());
                HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                boolean isRightHand = arm == HumanoidArm.RIGHT;
                Vec3 side = Vec3.directionFromRotation(0, player.getYRot() + (isRightHand ? 90 : -90));
                Vec3 down = Vec3.directionFromRotation(player.getXRot() + 90, player.getYRot());
                fire(player, front, side.add(down).scale(0.15));
            }
            player.playSound(fireSound(), 3.5f, 1);

            setLoaded(stack, false);
            stack.hurtAndBreak(1, player, (entity) -> {
                entity.broadcastBreakEvent(hand);
            });

            player.releaseUsingItem();
            if (worldIn.isClientSide) setActiveStack(hand, stack);

            return InteractionResultHolder.consume(stack);

        } else if (haveAmmo) {
            setLoadingStage(stack, 1);

            player.startUsingItem(hand);
            if (worldIn.isClientSide) setActiveStack(hand, stack);

            return InteractionResultHolder.consume(stack);

        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        setLoadingStage(stack, 0);
    }

    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int timeLeft) {
        int usingDuration = getUseDuration(stack) - timeLeft;
        int loadingStage = getLoadingStage(stack);

        if (loadingStage == 1 && usingDuration >= LOADING_STAGE_1) {
            entity.playSound(Sounds.MUSKET_LOAD_0, 0.8f, 1);
            setLoadingStage(stack, 2);

        } else if (loadingStage == 2 && usingDuration >= LOADING_STAGE_2) {
            entity.playSound(Sounds.MUSKET_LOAD_1, 0.8f, 1);
            setLoadingStage(stack, 3);

        } else if (loadingStage == 3 && usingDuration >= LOADING_STAGE_3) {
            entity.playSound(Sounds.MUSKET_LOAD_2, 0.8f, 1);
            setLoadingStage(stack, 4);
        }

        if (world.isClientSide && entity instanceof Player) {
            setActiveStack(entity.getUsedItemHand(), stack);
            return;
        }

        if (usingDuration >= RELOAD_DURATION && !isLoaded(stack)) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (!player.getAbilities().instabuild) {
                    ItemStack ammoStack = findAmmo(player);
                    if (ammoStack.isEmpty()) return;

                    ammoStack.shrink(1);
                    if (ammoStack.isEmpty()) player.getInventory().removeItem(ammoStack);
                }
            }

            // played on server
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), Sounds.MUSKET_READY, entity.getSoundSource(), 0.8f, 1);
            setLoaded(stack, true);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity enemy, LivingEntity entityIn) {
        stack.hurtAndBreak(1, entityIn, (entity) -> {
            entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityIn) {
        if (state.getDestroySpeed(worldIn, pos) != 0) {
            stack.hurtAndBreak(1, entityIn, (entity) -> {
                entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public void fire(LivingEntity shooter, Vec3 direction) {
        fire(shooter, direction, Vec3.ZERO);
    }

    public void fire(LivingEntity shooter, Vec3 direction, Vec3 smokeOriginOffset) {
        RandomSource random = shooter.getRandom();
        Level level = shooter.level;

        float angle = (float) Math.PI * 2 * random.nextFloat();
        float gaussian = Math.abs((float) random.nextGaussian());
        if (gaussian > 4) gaussian = 4;

        float spread = bulletStdDev() * gaussian;

        // a plane perpendicular to direction
        Vec3 n1;
        Vec3 n2;
        if (Math.abs(direction.x) < 1e-5 && Math.abs(direction.z) < 1e-5) {
            n1 = new Vec3(1, 0, 0);
            n2 = new Vec3(0, 0, 1);
        } else {
            n1 = new Vec3(-direction.z, 0, direction.x).normalize();
            n2 = direction.cross(n1);
        }

        Vec3 motion = direction.scale(Mth.cos(spread))
            .add(n1.scale(Mth.sin(spread) * Mth.sin(angle))) // signs are not important for random angle
            .add(n2.scale(Mth.sin(spread) * Mth.cos(angle)))
            .scale(bulletSpeed());

        Vec3 origin = new Vec3(shooter.getX(), shooter.getEyeY(), shooter.getZ());

        BulletEntity bullet = new BulletEntity(level);
        bullet.setOwner(shooter);
        bullet.setPos(origin);
        bullet.setInitialSpeed(bulletSpeed());
        bullet.setDeltaMovement(motion);
        float t = random.nextFloat();
        bullet.damageMultiplier = t * damageMultiplierMin() + (1 - t) * damageMultiplierMax();
        bullet.ignoreInvulnerableTime = ignoreInvulnerableTime();

        level.addFreshEntity(bullet);
        MusketMod.sendSmokeEffect(shooter, origin.add(smokeOriginOffset), direction);
    }

    public static void fireParticles(Level world, Vec3 origin, Vec3 direction) {
        RandomSource random = RandomSource.create();

        for (int i = 0; i != 10; ++i) {
            double t = Math.pow(random.nextFloat(), 1.5);
            Vec3 p = origin.add(direction.scale(1.25 + t));
            p = p.add(new Vec3(random.nextFloat() - 0.5, random.nextFloat() - 0.5, random.nextFloat() - 0.5).scale(0.1));
            Vec3 v = direction.scale(0.1 * (1 - t));
            world.addParticle(ParticleTypes.POOF, p.x, p.y, p.z, v.x, v.y, v.z);
        }
    }

    // for Wastelands of Baedoor
    public static void increaseGunExperience(Player player) {
        final String NAME = "gun_experience";
        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective(NAME);
        if (objective == null) {
            objective = board.addObjective(NAME, ObjectiveCriteria.DUMMY, Component.literal(NAME), ObjectiveCriteria.RenderType.INTEGER);
        }
        Score score = board.getOrCreatePlayerScore(player.getScoreboardName(), objective);
        score.increment();
    }

    public static ItemStack getActiveStack(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return activeMainHandStack;
        } else {
            return activeOffhandStack;
        }
    }

    public static void setActiveStack(InteractionHand hand, ItemStack stack) {
        if (hand == InteractionHand.MAIN_HAND) {
            activeMainHandStack = stack;
        } else {
            activeOffhandStack = stack;
        }
    }

    public static boolean isAmmo(ItemStack stack) {
        return stack.getItem() == Items.CARTRIDGE;
    }

    public static ItemStack findAmmo(Player player) {
        if (isAmmo(player.getItemBySlot(EquipmentSlot.OFFHAND))) {
            return player.getItemBySlot(EquipmentSlot.OFFHAND);

        } else if (isAmmo(player.getItemBySlot(EquipmentSlot.MAINHAND))) {
            return player.getItemBySlot(EquipmentSlot.MAINHAND);

        } else {
            for (int i = 0; i != player.getInventory().getContainerSize(); ++i) {
                ItemStack itemstack = player.getInventory().getItem(i);
                if (isAmmo(itemstack)) return itemstack;
            }

            return ItemStack.EMPTY;
        }
    }

    public static boolean isLoaded(ItemStack stack) {
        return stack.getOrCreateTag().getByte("loaded") != 0;
    }

    public static void setLoaded(ItemStack stack, boolean loaded) {
        if (loaded) {
            stack.getOrCreateTag().putByte("loaded", (byte)1);
        } else {
            stack.getOrCreateTag().remove("loaded");
        }
    }

    public static int getLoadingStage(ItemStack stack) {
        return stack.getOrCreateTag().getInt("loadingStage");
    }

    public static void setLoadingStage(ItemStack stack, int loadingStage) {
        if (loadingStage != 0) {
            stack.getOrCreateTag().putInt("loadingStage", loadingStage);
        } else {
            stack.getOrCreateTag().remove("loadingStage");
        }
    }
}
