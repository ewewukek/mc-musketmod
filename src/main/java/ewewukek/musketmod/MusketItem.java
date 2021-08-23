package ewewukek.musketmod;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MusketItem extends Item {
    public static final int DURABILITY = 250;
    public static final int LOADING_STAGE_1 = 5;
    public static final int LOADING_STAGE_2 = 10;
    public static final int LOADING_STAGE_3 = 20;
    public static final int RELOAD_DURATION = 30;

    public static float bulletStdDev;
    public static double bulletSpeed;

    public MusketItem(Item.Properties properties) {
        super(properties.defaultDurability(DURABILITY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return super.use(worldIn, player, hand);

        ItemStack stack = player.getItemInHand(hand);
        boolean creative = player.getAbilities().instabuild;

        if (player.isEyeInFluid(FluidTags.WATER) && !creative) {
            return InteractionResultHolder.fail(stack);
        }

        boolean haveAmmo = !findAmmo(player).isEmpty() || creative;
        boolean loaded = isLoaded(stack);

        if (loaded && isReady(stack)) {
            if (!worldIn.isClientSide) {
                fireBullet(worldIn, player);
            }
            player.playSound(MusketMod.SOUND_MUSKET_FIRE, 3.5f, 1);

            damageItem(stack, player);
            setReady(stack, false);
            setLoaded(stack, false);

            return InteractionResultHolder.consume(stack);

        } else if (loaded || haveAmmo) {
            if (!loaded) {
                setLoadingStage(stack, 0);
            }
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);

        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (isLoaded(stack)) setReady(stack, true);
    }

    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int timeLeft) {
        if (world.isClientSide || !(entity instanceof Player)) return;

        Player player = (Player) entity;
        int usingDuration = getUseDuration(stack) - timeLeft;
        int loadingStage = getLoadingStage(stack);

        double posX = player.getX();
        double posY = player.getY();
        double posZ = player.getZ();

        if (loadingStage == 0 && usingDuration >= LOADING_STAGE_1) {
            world.playSound(null, posX, posY, posZ, MusketMod.SOUND_MUSKET_LOAD_0, SoundSource.PLAYERS, 0.8f, 1);
            setLoadingStage(stack, 1);

        } else if (loadingStage == 1 && usingDuration >= LOADING_STAGE_2) {
            world.playSound(null, posX, posY, posZ, MusketMod.SOUND_MUSKET_LOAD_1, SoundSource.PLAYERS, 0.8f, 1);
            setLoadingStage(stack, 2);

        } else if (loadingStage == 2 && usingDuration >= LOADING_STAGE_3) {
            world.playSound(null, posX, posY, posZ, MusketMod.SOUND_MUSKET_LOAD_2, SoundSource.PLAYERS, 0.8f, 1);
            setLoadingStage(stack, 3);
        }

        if (usingDuration >= RELOAD_DURATION && !isLoaded(stack)) {
            if (!player.getAbilities().instabuild) {
                ItemStack ammoStack = findAmmo(player);
                if (ammoStack.isEmpty()) return;

                ammoStack.shrink(1);
                if (ammoStack.isEmpty()) player.getInventory().removeItem(ammoStack);
            }

            world.playSound(null, posX, posY, posZ, MusketMod.SOUND_MUSKET_READY, SoundSource.PLAYERS, 0.8f, 1);
            setLoaded(stack, true);
        }
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!worldIn.isClientSide && entityLiving instanceof Player && state.getDestroySpeed(worldIn, pos) != 0) {
            damageItem(stack, (Player) entityLiving);
        }
        return false;
    }

    public static void damageItem(ItemStack stack, Player player) {
        stack.hurtAndBreak(1, player, (entity) -> {
            entity.broadcastBreakEvent(player.getUsedItemHand());
        });
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    private boolean isAmmo(ItemStack stack) {
        return stack.getItem() == MusketMod.CARTRIDGE;
    }

    private ItemStack findAmmo(Player player) {
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

    private void fireBullet(Level worldIn, Player player) {
        final float deg2rad = 0.017453292f;
        Vec3 front = new Vec3(0, 0, 1).xRot(-deg2rad * player.getXRot()).yRot(-deg2rad * player.getYRot());
        Vec3 pos = new Vec3(player.getX(), player.getEyeY(), player.getZ());

        float angle = (float) Math.PI * 2 * worldIn.getRandom().nextFloat();
        float gaussian = Math.abs((float) worldIn.getRandom().nextGaussian());
        if (gaussian > 4) gaussian = 4;

        front = front.xRot(bulletStdDev * gaussian * Mth.sin(angle))
                .yRot(bulletStdDev * gaussian * Mth.cos(angle));

        Vec3 motion = front.scale(bulletSpeed);

        Vec3 playerMotion = player.getDeltaMovement();
        motion.add(playerMotion.x, player.isOnGround() ? 0 : playerMotion.y, playerMotion.z);

        BulletEntity bullet = new BulletEntity(worldIn);
        bullet.setOwner(player);
        bullet.setPos(pos.x, pos.y, pos.z);
        bullet.setDeltaMovement(motion);

        worldIn.addFreshEntity(bullet);
    }

    public static boolean isLoaded(ItemStack stack) {
        return stack.getOrCreateTag().getByte("loaded") != 0;
    }

    private void setLoaded(ItemStack stack, boolean loaded) {
        if (loaded) {
            stack.getOrCreateTag().putByte("loaded", (byte)1);
        } else {
            stack.getOrCreateTag().remove("loaded");
        }
    }

    public static boolean isReady(ItemStack stack) {
        return stack.getOrCreateTag().getByte("ready") != 0;
    }

    private void setReady(ItemStack stack, boolean ready) {
        if (ready) {
            stack.getOrCreateTag().putByte("ready", (byte)1);
        } else {
            stack.getOrCreateTag().remove("ready");
        }
    }

    private int getLoadingStage(ItemStack stack) {
        return stack.getOrCreateTag().getInt("loadingStage");
    }

    private void setLoadingStage(ItemStack stack, int loadingStage) {
        if (loadingStage != 0) {
            stack.getOrCreateTag().putInt("loadingStage", loadingStage);
        } else {
            stack.getOrCreateTag().remove("loadingStage");
        }
    }
}
