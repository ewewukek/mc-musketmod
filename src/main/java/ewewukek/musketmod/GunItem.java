package ewewukek.musketmod;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public abstract class GunItem extends Item {
    public static final DataComponentType<Boolean> LOADED = new DataComponentType.Builder<Boolean>()
        .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();
    public static final DataComponentType<Byte> LOADING_STAGE = new DataComponentType.Builder<Byte>()
        .persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE).build();

    // for RenderHelper
    public static ItemStack activeMainHandStack;
    public static ItemStack activeOffhandStack;

    public GunItem(Properties properties) {
        super(properties);
    }

    public abstract float bulletStdDev();
    public abstract float bulletSpeed();
    public abstract float damage();
    public abstract SoundEvent fireSound(ItemStack stack);

    public int pelletCount() {
        return 1;
    }

    public boolean twoHanded() {
        return true;
    }

    public int hitDurabilityDamage() {
        return 1;
    }

    public static boolean canUse(LivingEntity entity) {
        boolean creative = entity instanceof Player player && player.getAbilities().instabuild;
        return creative || (!entity.isEyeInFluid(FluidTags.WATER) && !entity.isEyeInFluid(FluidTags.LAVA));
    }

    public boolean canUseFrom(LivingEntity entity, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return true;
        }
        if (twoHanded()) {
            return false;
        }
        // pistol in offhand is unusable if musket is equipped in main hand
        ItemStack stack = entity.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof GunItem gun) {
            return !gun.twoHanded();
        }
        return true;
    }

    public static boolean isInHand(LivingEntity entity, InteractionHand hand) {
        ItemStack stack = entity.getItemInHand(hand);
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof GunItem gun) {
            return gun.canUseFrom(entity, hand);
        }
        return false;
    }

    public static boolean isHoldingGun(LivingEntity entity) {
        return getHoldingHand(entity) != null;
    }

    @Nullable
    public static InteractionHand getHoldingHand(LivingEntity entity) {
        if (isInHand(entity, InteractionHand.MAIN_HAND)) return InteractionHand.MAIN_HAND;
        if (isInHand(entity, InteractionHand.OFF_HAND)) return InteractionHand.OFF_HAND;
        return null;
    }

    public Vec3 smokeOffsetFor(LivingEntity entity, HumanoidArm arm) {
        boolean isRightHand = arm == HumanoidArm.RIGHT;
        Vec3 side = Vec3.directionFromRotation(0, entity.getYRot() + (isRightHand ? 90 : -90));
        Vec3 down = Vec3.directionFromRotation(entity.getXRot() + 90, entity.getYRot());
        return side.add(down).scale(0.15);
    }

    public static boolean hasFlame(ItemStack stack) {
        return VanillaHelper.getEnchantmentLevel(stack, Enchantments.FLAME) > 0;
    }

    public static boolean hasInfinity(ItemStack stack) {
        return VanillaHelper.getEnchantmentLevel(stack, Enchantments.INFINITY) > 0;
    }

    public static int getPowerLevel(ItemStack stack) {
        return VanillaHelper.getEnchantmentLevel(stack, Enchantments.POWER);
    }

    public static int getQuickChargeLevel(ItemStack stack) {
        return VanillaHelper.getEnchantmentLevel(stack, Enchantments.QUICK_CHARGE);
    }

    public static Pair<Integer, Integer> getLoadingDuration(ItemStack stack) {
        int level = getQuickChargeLevel(stack);
        int stages = Config.loadingStages;
        float total = stages * Config.loadingStageDuration;
        float reduction = level * Config.reductionPerQuickChargeLevel;
        float duration = (total - reduction) / stages;
        if (duration < 0.25f) duration = 0.25f;
        if (level == 3) stages--;
        return Pair.of(stages, (int)(20 * duration));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUse(player) || !canUseFrom(player, hand)) {
            return InteractionResultHolder.pass(stack);
        }

        if (isLoaded(stack)) {
            if (!level.isClientSide) {
                Vec3 direction = Vec3.directionFromRotation(player.getXRot(), player.getYRot());
                HumanoidArm arm = hand == InteractionHand.MAIN_HAND
                    ? player.getMainArm() : player.getMainArm().getOpposite();
                fire(player, stack, direction, smokeOffsetFor(player, arm));
            }
            player.playSound(fireSound(stack), 3.5f, 1);

            setLoaded(stack, false);
            stack.hurtAndBreak(1, player, Player.getSlotForHand(hand));

            player.releaseUsingItem();
            if (level.isClientSide) setActiveStack(hand, stack);

            return InteractionResultHolder.consume(stack);

        } else if (hand == InteractionHand.MAIN_HAND) {
            // shoot from offhand if it's loaded
            ItemStack offhandStack = player.getOffhandItem();
            if (offhandStack.getItem() instanceof GunItem offhandGun && isLoaded(offhandStack)
            && offhandGun.canUseFrom(player, InteractionHand.OFF_HAND)) {

                return InteractionResultHolder.pass(stack);
            }
        }

        if (getLoadingStage(stack) == 0) {
            if (!checkAmmo(player, stack)) {
                return InteractionResultHolder.fail(stack);
            }
            setLoadingStage(stack, 1);

        } else { // skip stage duration if it's last one (cocking the hammer)
            int loadingStages = getLoadingDuration(stack).getLeft();
            if (getLoadingStage(stack) == loadingStages)
                setLoadingStage(stack, loadingStages + 1);
        }

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    public static Vec3 addSpread(Vec3 direction, RandomSource random, float spreadStdDev) {
        float gaussian = Math.abs((float)random.nextGaussian());
        if (gaussian > 4) gaussian = 4;
        float error = (float)Math.toRadians(spreadStdDev) * gaussian;
        return applyError(direction, random, error);
    }

    public static Vec3 addUniformSpread(Vec3 direction, RandomSource random, float spread) {
        float error = (float)Math.toRadians(spread) * random.nextFloat();
        return applyError(direction, random, error);
    }

    public static Vec3 applyError(Vec3 direction, RandomSource random, float coneAngle) {
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

        float angle = (float)Math.PI * 2 * random.nextFloat();
        // signs are not important for random angle
        return direction.scale(Mth.cos(coneAngle))
            .add(n1.scale(Mth.sin(coneAngle) * Mth.sin(angle)))
            .add(n2.scale(Mth.sin(coneAngle) * Mth.cos(angle)));
    }

    public Vec3 aimAt(LivingEntity entity, LivingEntity target) {
        double dist = entity.distanceTo(target);
        double ticks = 20 * dist / bulletSpeed();
        // predicted bullet drop
        double bulletDrop = 0.5 * ticks * ticks * BulletEntity.GRAVITY;
        Vec3 pos = new Vec3(
            target.getX(),
            0.5 * (target.getEyeY() + target.getY(0.5)),
            target.getZ()
           );
        return new Vec3(
            pos.x() - entity.getX(),
            pos.y() + bulletDrop - entity.getEyeY(),
            pos.z() - entity.getZ()
        ).normalize();
    }

    public void mobUse(LivingEntity entity, InteractionHand hand, Vec3 direction) {
        ItemStack stack = entity.getItemInHand(hand);
        HumanoidArm arm = entity.getMainArm();
        if (hand == InteractionHand.OFF_HAND) arm = arm.getOpposite();
        mobUse(entity, stack, direction, smokeOffsetFor(entity, arm));
    }

    public void mobUse(LivingEntity entity, ItemStack stack, Vec3 direction, Vec3 smokeOffset) {
        Level level = entity.level();
        if (level.isClientSide) return;
        if (!isLoaded(stack)) return;

        fire(entity, stack, direction, smokeOffset);
        entity.playSound(fireSound(stack), 3.5f, 1);
        setLoaded(stack, false);
    }

    public static int reloadDuration(ItemStack stack) {
        Pair<Integer, Integer> loadingDuration = getLoadingDuration(stack);
        int loadingStages = loadingDuration.getLeft();
        int ticksPerLoadingStage = loadingDuration.getRight();

        int loadingStagesRemaining = 1 + loadingStages - getLoadingStage(stack);
        return loadingStagesRemaining * ticksPerLoadingStage;
    }

    public static boolean checkAmmo(Player player, ItemStack stack) {
        if (player.getAbilities().instabuild || hasInfinity(stack)) return true;
        ItemStack ammoStack = findAmmo(player);
        return !ammoStack.isEmpty();
    }

    public static void consumeAmmo(Player player, ItemStack stack) {
        if (player.getAbilities().instabuild || hasInfinity(stack)) return;

        ItemStack ammoStack = findAmmo(player);
        ammoStack.shrink(1);
        if (ammoStack.isEmpty()) {
            player.getInventory().removeItem(ammoStack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int ticksLeft) {
        if (isLoaded(stack)) {
            setLoadingStage(stack, 0);

        } else {
            int usingTicks = getUseDuration(stack, entity) - ticksLeft;
            int ticksPerLoadingStage = getLoadingDuration(stack).getRight();
            int prevLoadingStage = getLoadingStage(stack);
            int loadingStage = prevLoadingStage + usingTicks / ticksPerLoadingStage;

            if (prevLoadingStage == 1) {
                if (loadingStage == 1) {
                    setLoadingStage(stack, 0);

                } else if (!isLoaded(stack) && entity instanceof Player player){
                    consumeAmmo(player, stack);
                }
            }
            setLoadingStage(stack, loadingStage);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksLeft) {
        Pair<Integer, Integer> loadingDuration = getLoadingDuration(stack);
        int loadingStages = loadingDuration.getLeft();
        int ticksPerLoadingStage = loadingDuration.getRight();

        int usingTicks = getUseDuration(stack, entity) - ticksLeft;
        int prevLoadingStage = getLoadingStage(stack);
        int loadingStage = prevLoadingStage + usingTicks / ticksPerLoadingStage;

        if (loadingStage < loadingStages && usingTicks == ticksPerLoadingStage / 2) {
            entity.playSound(Sounds.MUSKET_LOAD_0, 0.8f, 1);
        }
        if (usingTicks > 0 && usingTicks % ticksPerLoadingStage == 0) {
            if (loadingStage < loadingStages) {
                entity.playSound(Sounds.MUSKET_LOAD_1, 0.8f, 1);
            } else if (loadingStage == loadingStages) {
                entity.playSound(Sounds.MUSKET_LOAD_2, 0.8f, 1);
            }
        }

        if (level.isClientSide && entity instanceof Player) {
            setActiveStack(entity.getUsedItemHand(), stack);
            return;
        }

        if (loadingStage > loadingStages && !isLoaded(stack)) {
            // played on server
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), Sounds.MUSKET_READY, entity.getSoundSource(), 0.8f, 1);
            if (prevLoadingStage == 1 && entity instanceof Player player) {
                consumeAmmo(player, stack);
            }
            setLoaded(stack, true);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity entity) {
        stack.hurtAndBreak(hitDurabilityDamage(), entity, EquipmentSlot.MAINHAND);
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity entity) {
        if (blockState.getDestroySpeed(level, blockPos) != 0) {
            stack.hurtAndBreak(hitDurabilityDamage(), entity, EquipmentSlot.MAINHAND);
        }
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    public void fire(LivingEntity entity, ItemStack stack, Vec3 direction, Vec3 smokeOffset) {
        Level level = entity.level();
        Vec3 origin = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        boolean flame = hasFlame(stack);
        int power = getPowerLevel(stack);

        for (int i = 0; i < pelletCount(); i++) {
            BulletEntity bullet = new BulletEntity(level);
            bullet.setOwner(entity);
            bullet.setPos(origin);
            bullet.setVelocity(bulletSpeed(), addSpread(direction, entity.getRandom(), bulletStdDev()));
            float damage = damage() + Config.randomDamage * entity.getRandom().nextFloat();
            bullet.setDamage(bulletSpeed(), damage);
            bullet.setPelletCount(pelletCount());
            if (flame) {
                bullet.igniteForSeconds(100.0f);
                bullet.setSharedFlagOnFire(true);
            }
            bullet.setPowerLevel(power);

            level.addFreshEntity(bullet);
        }

        MusketMod.sendSmokeEffect((ServerLevel)level, origin.add(smokeOffset), direction);
    }

    public static void fireParticles(Level level, Vec3 origin, Vec3 direction) {
        RandomSource random = RandomSource.create();

        for (int i = 0; i < 10; i++) {
            double t = Math.pow(random.nextFloat(), 1.5);
            Vec3 p = origin.add(direction.scale(1.25 + t));
            p = p.add(new Vec3(random.nextFloat() - 0.5, random.nextFloat() - 0.5, random.nextFloat() - 0.5).scale(0.1));
            Vec3 v = direction.scale(0.1 * (1 - t));
            level.addParticle(ParticleTypes.POOF, p.x, p.y, p.z, v.x, v.y, v.z);
        }
    }

    // for Wastelands of Baedoor
    public static void increaseGunExperience(Player player) {
        final String NAME = "gun_experience";
        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective(NAME);
        if (objective == null) {
            objective = board.addObjective(NAME, ObjectiveCriteria.DUMMY, Component.literal(NAME),
                ObjectiveCriteria.RenderType.INTEGER, true, null);
        }
        ScoreAccess score = board.getOrCreatePlayerScore(player, objective);
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
        ItemStack stack = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (isAmmo(stack)) return stack;

        stack = player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (isAmmo(stack)) return stack;

        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            stack = player.getInventory().getItem(i);
            if (isAmmo(stack)) return stack;
        }

        return ItemStack.EMPTY;
    }

    public static boolean isReady(ItemStack stack) {
        return isLoaded(stack) && getLoadingStage(stack) == 0;
    }

    public static boolean isLoaded(ItemStack stack) {
        Boolean loaded = stack.get(LOADED);
        return loaded != null && loaded;
    }

    public static void setLoaded(ItemStack stack, boolean loaded) {
        if (loaded) {
            stack.set(LOADED, true);
        } else {
            stack.remove(LOADED);
        }
    }

    public static int getLoadingStage(ItemStack stack) {
        Byte loadingStage = stack.get(LOADING_STAGE);
        return loadingStage != null ? loadingStage : 0;
    }

    public static void setLoadingStage(ItemStack stack, int loadingStage) {
        if (loadingStage > 0) {
            stack.set(LOADING_STAGE, (byte)loadingStage);
        } else {
            stack.remove(LOADING_STAGE);
        }
    }
}
