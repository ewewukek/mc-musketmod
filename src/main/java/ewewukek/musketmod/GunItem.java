package ewewukek.musketmod;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public abstract class GunItem extends Item {
    public static final int TICKS_PER_LOADING_STAGE = 10;
    public static final int LOADING_STAGES = 3;

    public static final TagKey<Enchantment> FLAME_ENCHANTMENT = TagKey.create(Registries.ENCHANTMENT, MusketMod.resource("flame"));
    public static final TagKey<Enchantment> INFINITY_ENCHANTMENT = TagKey.create(Registries.ENCHANTMENT, MusketMod.resource("infinity"));
    public static final TagKey<Enchantment> POWER_ENCHANTMENT = TagKey.create(Registries.ENCHANTMENT, MusketMod.resource("power"));

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
    public abstract float damageMin();
    public abstract float damageMax();
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
        return EnchantmentHelper.hasTag(stack, FLAME_ENCHANTMENT);
    }

    public static boolean infiniteAmmo(ItemStack stack) {
        return EnchantmentHelper.hasTag(stack, INFINITY_ENCHANTMENT);
    }

    public static int getPowerLevel(ItemStack stack) {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            if (entry.getKey().is(POWER_ENCHANTMENT)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!canUseFrom(player, hand)) return super.use(level, player, hand);

        ItemStack stack = player.getItemInHand(hand);
        boolean creative = player.getAbilities().instabuild;

        if (!creative && (player.isEyeInFluid(FluidTags.WATER) || player.isEyeInFluid(FluidTags.LAVA))) {
            return InteractionResultHolder.fail(stack);
        }

        // shoot from left hand if both are loaded
        if (hand == InteractionHand.MAIN_HAND && !twoHanded() && isLoaded(stack)) {
            ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof GunItem offhandGun) {
                if (!offhandGun.twoHanded() && isLoaded(offhandStack)) {
                    return InteractionResultHolder.pass(stack);
                }
            }
        }

        if (isLoaded(stack)) {
            if (!level.isClientSide) {
                Vec3 direction = Vec3.directionFromRotation(player.getXRot(), player.getYRot());
                HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                fire(player, stack, direction, smokeOffsetFor(player, arm));
            }
            player.playSound(fireSound(stack), 3.5f, 1);

            setLoaded(stack, false);
            stack.hurtAndBreak(1, player, Player.getSlotForHand(hand));

            player.releaseUsingItem();
            if (level.isClientSide) setActiveStack(hand, stack);

            return InteractionResultHolder.consume(stack);

        }

        if (getLoadingStage(stack) == 0) {
            if (!creative && !infiniteAmmo(stack)) {
                ItemStack ammoStack = findAmmo(player);
                if (ammoStack.isEmpty()) {
                    return InteractionResultHolder.fail(stack);
                }
                ammoStack.shrink(1);
                if (ammoStack.isEmpty()) {
                    player.getInventory().removeItem(ammoStack);
                }
            }
            setLoadingStage(stack, 1);
        } else if (getLoadingStage(stack) == LOADING_STAGES) {
            setLoadingStage(stack, LOADING_STAGES + 1);
        }

        player.startUsingItem(hand);
        if (level.isClientSide) setActiveStack(hand, stack);

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
        int loadingStagesLeft = 1 + LOADING_STAGES - getLoadingStage(stack);
        return loadingStagesLeft * TICKS_PER_LOADING_STAGE;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int ticksLeft) {
        if (isLoaded(stack)) {
            setLoadingStage(stack, 0);

        } else {
            int useTicks = getUseDuration(stack, entity) - ticksLeft;
            int loadingStage = getLoadingStage(stack) + useTicks / TICKS_PER_LOADING_STAGE;
            setLoadingStage(stack, loadingStage);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksLeft) {
        int useTicks = getUseDuration(stack, entity) - ticksLeft;
        int loadingStage = getLoadingStage(stack) + useTicks / TICKS_PER_LOADING_STAGE;

        if (loadingStage < LOADING_STAGES && useTicks == TICKS_PER_LOADING_STAGE / 2) {
            entity.playSound(Sounds.MUSKET_LOAD_0, 0.8f, 1);
        }
        if (useTicks > 0 && useTicks % TICKS_PER_LOADING_STAGE == 0) {
            if (loadingStage < LOADING_STAGES) {
                entity.playSound(Sounds.MUSKET_LOAD_1, 0.8f, 1);
            } else if (loadingStage == LOADING_STAGES) {
                entity.playSound(Sounds.MUSKET_LOAD_2, 0.8f, 1);
            }
        }

        if (level.isClientSide && entity instanceof Player) {
            setActiveStack(entity.getUsedItemHand(), stack);
            return;
        }

        if (loadingStage > LOADING_STAGES && !isLoaded(stack)) {
            // played on server
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), Sounds.MUSKET_READY, entity.getSoundSource(), 0.8f, 1);
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
            bullet.setPelletCount(pelletCount());
            bullet.setVelocity(bulletSpeed(), addSpread(direction, entity.getRandom(), bulletStdDev()));
            bullet.setDamage(bulletSpeed(), damageMin(), damageMax());
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
