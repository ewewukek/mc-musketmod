package ewewukek.musketmod;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class GunItem extends Item {
    public static final int LOADING_STAGE_1 = 5;
    public static final int LOADING_STAGE_2 = 10;
    public static final int LOADING_STAGE_3 = 20;
    public static final int RELOAD_DURATION = 30;

    // for RenderHelper
    public static ItemStack activeMainHandStack;
    public static ItemStack activeOffhandStack;

    public GunItem(Item.Properties properties) {
        super(properties);
    }

    public abstract float bulletStdDev();
    public abstract float bulletSpeed();
    public abstract float damageMultiplierMin();
    public abstract float damageMultiplierMax();
    public abstract SoundEvent fireSound();
    public abstract boolean twoHanded();

    public boolean canUseFrom(PlayerEntity player, Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return true;
        }
        if (twoHanded()) {
            return false;
        }
        ItemStack mainHandStack = player.getHeldItem(Hand.MAIN_HAND);
        if (!mainHandStack.isEmpty() && mainHandStack.getItem() instanceof GunItem) {
            return !((GunItem)mainHandStack.getItem()).twoHanded();
        }
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity player, Hand hand) {
        if (!canUseFrom(player, hand)) return super.onItemRightClick(worldIn, player, hand);

        ItemStack stack = player.getHeldItem(hand);
        boolean creative = player.abilities.isCreativeMode;

        if (player.areEyesInFluid(FluidTags.WATER) && !creative) {
            return ActionResult.resultFail(stack);
        }

        // shoot from left hand if both are loaded
        if (hand == Hand.MAIN_HAND && !twoHanded() && isLoaded(stack)) {
            ItemStack offhandStack = player.getHeldItem(Hand.OFF_HAND);
            if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof GunItem) {
                GunItem offhandGun = (GunItem)offhandStack.getItem();
                if (!offhandGun.twoHanded() && isLoaded(offhandStack)) {
                    return ActionResult.resultPass(stack);
                }
            }
        }
        boolean haveAmmo = !findAmmo(player).isEmpty() || creative;
        boolean loaded = isLoaded(stack);

        if (loaded) {
            if (!worldIn.isRemote) {
                Vector3d front = Vector3d.fromPitchYaw(player.rotationPitch, player.rotationYaw);
                HandSide arm = hand == Hand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
                boolean isRightHand = arm == HandSide.RIGHT;
                Vector3d side = Vector3d.fromPitchYaw(0, player.rotationYaw + (isRightHand ? 90 : -90));
                Vector3d down = Vector3d.fromPitchYaw(player.rotationPitch + 90, player.rotationYaw);
                fire(player, front, side.add(down).scale(0.15));
            }
            player.playSound(fireSound(), 3.5f, 1);

            setLoaded(stack, false);
            stack.damageItem(1, player, (entity) -> {
                entity.sendBreakAnimation(hand);
            });

            if (worldIn.isRemote) setActiveStack(hand, stack);

            return ActionResult.resultConsume(stack);

        } else if (haveAmmo) {
            setLoadingStage(stack, 1);

            player.setActiveHand(hand);
            if (worldIn.isRemote) setActiveStack(hand, stack);

            return ActionResult.resultConsume(stack);

        } else {
            return ActionResult.resultFail(stack);
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        setLoadingStage(stack, 0);
    }

    @Override
    public void onUse(World world, LivingEntity entity, ItemStack stack, int timeLeft) {
        int usingDuration = getUseDuration(stack) - timeLeft;
        int loadingStage = getLoadingStage(stack);

        if (loadingStage == 1 && usingDuration >= LOADING_STAGE_1) {
            entity.playSound(MusketMod.SOUND_MUSKET_LOAD_0, 0.8F, 1.0F);
            setLoadingStage(stack, 2);

        } else if (loadingStage == 2 && usingDuration >= LOADING_STAGE_2) {
            entity.playSound(MusketMod.SOUND_MUSKET_LOAD_1, 0.8F, 1.0F);
            setLoadingStage(stack, 3);

        } else if (loadingStage == 3 && usingDuration >= LOADING_STAGE_3) {
            entity.playSound(MusketMod.SOUND_MUSKET_LOAD_2, 0.8F, 1.0F);
            setLoadingStage(stack, 4);
        }

        if (world.isRemote && entity instanceof PlayerEntity) {
            setActiveStack(entity.getActiveHand(), stack);
            return;
        }

        if (usingDuration >= RELOAD_DURATION && !isLoaded(stack)) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)entity;
                if (!player.abilities.isCreativeMode) {
                    ItemStack ammoStack = findAmmo(player);
                    if (ammoStack.isEmpty()) return;

                    ammoStack.shrink(1);
                    if (ammoStack.isEmpty()) player.inventory.deleteStack(ammoStack);
                }
            }

            world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), MusketMod.SOUND_MUSKET_READY, entity.getSoundCategory(), 0.8F, 1.0F);
            setLoaded(stack, true);
        }
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity enemy, LivingEntity entityIn) {
        stack.damageItem(1, entityIn, (entity) -> {
            entity.sendBreakAnimation(EquipmentSlotType.MAINHAND);
        });
        return false;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityIn) {
        if (state.getBlockHardness(worldIn, pos) != 0) {
            stack.damageItem(1, entityIn, (entity) -> {
                entity.sendBreakAnimation(EquipmentSlotType.MAINHAND);
            });
        }
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public static ItemStack getActiveStack(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return activeMainHandStack;
        } else {
            return activeOffhandStack;
        }
    }

    public static void setActiveStack(Hand hand, ItemStack stack) {
        if (hand == Hand.MAIN_HAND) {
            activeMainHandStack = stack;
        } else {
            activeOffhandStack = stack;
        }
    }
    public static boolean isLoaded(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.getByte("loaded") == 1;
    }

    public static boolean isAmmo(ItemStack stack) {
        return stack.getItem() == MusketMod.CARTRIDGE;
    }

    public static ItemStack findAmmo(PlayerEntity player) {
        if (isAmmo(player.getHeldItem(Hand.OFF_HAND))) {
            return player.getHeldItem(Hand.OFF_HAND);

        } else if (isAmmo(player.getHeldItem(Hand.MAIN_HAND))) {
            return player.getHeldItem(Hand.MAIN_HAND);

        } else {
            for (int i = 0; i != player.inventory.mainInventory.size(); ++i) {
                ItemStack itemstack = player.inventory.mainInventory.get(i);
                if (isAmmo(itemstack)) return itemstack;
            }

            return ItemStack.EMPTY;
        }
    }

    public void fire(LivingEntity shooter, Vector3d direction) {
        fire(shooter, direction, Vector3d.ZERO);
    }

    public void fire(LivingEntity shooter, Vector3d direction, Vector3d smokeOriginOffset) {
        float angle = (float) Math.PI * 2 * random.nextFloat();
        float gaussian = Math.abs((float) random.nextGaussian());
        if (gaussian > 4) gaussian = 4;

        float spread = bulletStdDev() * gaussian;

        // a plane perpendicular to direction
        Vector3d n1;
        Vector3d n2;
        if (Math.abs(direction.x) < 1e-5 && Math.abs(direction.z) < 1e-5) {
            n1 = new Vector3d(1, 0, 0);
            n2 = new Vector3d(0, 0, 1);
        } else {
            n1 = new Vector3d(-direction.z, 0, direction.x).normalize();
            n2 = direction.crossProduct(n1);
        }

        Vector3d motion = direction.scale(MathHelper.cos(spread))
            .add(n1.scale(MathHelper.sin(spread) * MathHelper.sin(angle))) // signs are not important for random angle
            .add(n2.scale(MathHelper.sin(spread) * MathHelper.cos(angle)))
            .scale(bulletSpeed());

        Vector3d origin = new Vector3d(shooter.getPosX(), shooter.getPosY() + shooter.getEyeHeight(), shooter.getPosZ());

        BulletEntity bullet = new BulletEntity(shooter.world);
        bullet.setShooter(shooter);
        bullet.setPosition(origin.x, origin.y, origin.z);
        bullet.setMotion(motion);
        float t = random.nextFloat();
        bullet.damageMultiplier = t * damageMultiplierMin() + (1 - t) * damageMultiplierMax();

        shooter.world.addEntity(bullet);
        MusketMod.sendSmokeEffect(shooter, origin.add(smokeOriginOffset), direction);
    }

    public static void fireParticles(World world, Vector3d origin, Vector3d direction) {
        Random random = world.getRandom();

        for (int i = 0; i != 10; ++i) {
            double t = Math.pow(random.nextFloat(), 1.5);
            Vector3d p = origin.add(direction.scale(1.25 + t));
            p = p.add(new Vector3d(random.nextFloat() - 0.5, random.nextFloat() - 0.5, random.nextFloat() - 0.5).scale(0.1));
            Vector3d v = direction.scale(0.1).scale(1 - t);
            world.addParticle(ParticleTypes.POOF, p.x, p.y, p.z, v.x, v.y, v.z);
        }
    }

    public static void setLoaded(ItemStack stack, boolean loaded) {
        stack.getOrCreateTag().putByte("loaded", (byte) (loaded ? 1 : 0));
    }

    public static void setLoadingStage(ItemStack stack, int loadingStage) {
        stack.getOrCreateTag().putInt("loadingStage", loadingStage);
    }

    public static int getLoadingStage(ItemStack stack) {
        return stack.getOrCreateTag().getInt("loadingStage");
    }
}
