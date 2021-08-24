package ewewukek.musketmod;

import net.minecraft.core.BlockPos;
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

        if (loaded) {
            if (!worldIn.isClientSide) {
                fireBullet(worldIn, player);
            }
            player.playSound(MusketMod.SOUND_MUSKET_FIRE, 3.5f, 1);

            damageItem(stack, player);
            setLoaded(stack, false);

            return InteractionResultHolder.consume(stack);

        } else if (haveAmmo) {
            setLoadingStage(stack, 1);
            player.startUsingItem(hand);
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
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        int usingDuration = getUseDuration(stack) - timeLeft;
        int loadingStage = getLoadingStage(stack);

        if (loadingStage == 1 && usingDuration >= LOADING_STAGE_1) {
            player.playSound(MusketMod.SOUND_MUSKET_LOAD_0, 0.8f, 1);
            setLoadingStage(stack, 2);

        } else if (loadingStage == 2 && usingDuration >= LOADING_STAGE_2) {
            player.playSound(MusketMod.SOUND_MUSKET_LOAD_1, 0.8f, 1);
            setLoadingStage(stack, 3);

        } else if (loadingStage == 3 && usingDuration >= LOADING_STAGE_3) {
            player.playSound(MusketMod.SOUND_MUSKET_LOAD_2, 0.8f, 1);
            setLoadingStage(stack, 4);
        }

        if (world.isClientSide) return;

        if (usingDuration >= RELOAD_DURATION && !isLoaded(stack)) {
            if (!player.getAbilities().instabuild) {
                ItemStack ammoStack = findAmmo(player);
                if (ammoStack.isEmpty()) return;

                ammoStack.shrink(1);
                if (ammoStack.isEmpty()) player.getInventory().removeItem(ammoStack);
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), MusketMod.SOUND_MUSKET_READY, player.getSoundSource(), 0.8f, 1);
            setLoaded(stack, true);
        }
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (entityLiving instanceof Player && state.getDestroySpeed(worldIn, pos) != 0) {
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
        final float deg2rad = (float)Math.PI / 180;
        Vec3 direction = new Vec3(0, 0, 1).xRot(-deg2rad * player.getXRot()).yRot(-deg2rad * player.getYRot());
        Vec3 pos = new Vec3(player.getX(), player.getEyeY(), player.getZ());

        float angle = (float) Math.PI * 2 * worldIn.getRandom().nextFloat();
        float gaussian = Math.abs((float) worldIn.getRandom().nextGaussian());
        if (gaussian > 4) gaussian = 4;

        float spread = bulletStdDev * gaussian;

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
            .scale(bulletSpeed);

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
