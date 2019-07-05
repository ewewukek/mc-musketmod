package ewewukek.musketmod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

public class MusketItem extends Item {
    public static final int DURABILITY = 250;
    public static final int RELOAD_DURATION = 30;
    public static final int AIM_DURATION = 20;
    public static final float DISPERSION_MULTIPLIER = 3;
    public static final float DISPERSION_STD = (float)Math.toRadians(0.4);

    @ObjectHolder(MusketMod.MODID + ":cartridge")
    public static Item CARTRIDGE;

    @ObjectHolder(MusketMod.MODID + ":musket_ready")
    public static SoundEvent SOUND_MUSKET_READY;

    @ObjectHolder(MusketMod.MODID + ":musket_fire")
    public static SoundEvent SOUND_MUSKET_FIRE;

    public MusketItem(Item.Properties properties) {
        super(properties.defaultMaxDamage(DURABILITY));

        addPropertyOverride(new ResourceLocation("loaded"), (stack, world, player) -> {
            return isLoaded(stack) ? 1 : 0;
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);

        boolean haveAmmo = !findAmmo(player).isEmpty() || player.abilities.isCreativeMode;
        if (isLoaded(stack) || haveAmmo) {
            player.setActiveHand(hand);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);

        } else {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity)entityLiving;

        if (isReady(stack)) {
            if (!worldIn.isRemote) {
                float dispersion = DISPERSION_STD;

                float t = (float)(getUseDuration(stack) - timeLeft) / AIM_DURATION;
                if (t < 1) {
                    dispersion *= t + (1 - t) * DISPERSION_MULTIPLIER;
                }

                fireBullet(worldIn, player, dispersion);

            } else {
                fireParticles(worldIn, player);
            }
            player.playSound(SOUND_MUSKET_FIRE, 1.5f, 1);

            stack.damageItem(1, player, (entity) -> {
                entity.sendBreakAnimation(player.getActiveHand());
            });

            setReady(stack, false);
            setLoaded(stack, false);

        } else if (isLoaded(stack)) {
            setReady(stack, true);
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof PlayerEntity)) return;

        if (getUseDuration(stack) - timeLeft >= RELOAD_DURATION && !isReady(stack) && !isLoaded(stack)) {
            PlayerEntity player = (PlayerEntity)entityLiving;

            if (!player.abilities.isCreativeMode) {
                ItemStack ammoStack = findAmmo(player);
                if (ammoStack.isEmpty()) return;

                ammoStack.shrink(1);
                if (ammoStack.isEmpty()) player.inventory.deleteStack(ammoStack);
            }

            player.playSound(SOUND_MUSKET_READY, 0.5f, 1);
            setLoaded(stack, true);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        if (isReady(stack)) {
            return UseAction.BOW;
        } else {
            return isLoaded(stack) ? UseAction.NONE : UseAction.BLOCK;
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    private boolean isAmmo(ItemStack stack) {
        return stack.getItem() == CARTRIDGE;
    }

    private ItemStack findAmmo(PlayerEntity player) {
        if (isAmmo(player.getHeldItem(Hand.OFF_HAND))) {
            return player.getHeldItem(Hand.OFF_HAND);

        } else if (isAmmo(player.getHeldItem(Hand.MAIN_HAND))) {
            return player.getHeldItem(Hand.MAIN_HAND);

        } else {
            for (int i = 0; i != player.inventory.getSizeInventory(); ++i) {
                ItemStack itemstack = player.inventory.getStackInSlot(i);
                if (isAmmo(itemstack)) return itemstack;
            }

            return ItemStack.EMPTY;
        }
    }

    private Vec3d getPlayerFiringPoint(PlayerEntity player) {
        Vec3d side = Vec3d.fromPitchYaw(0, player.rotationYaw + 90);
        if (player.getActiveHand() == Hand.OFF_HAND) side = side.scale(-1);
        Vec3d down = Vec3d.fromPitchYaw(player.rotationPitch + 90, player.rotationYaw);
        return new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ)
                    .add(side.add(down).scale(0.1));
    }

    private void fireBullet(World worldIn, PlayerEntity player, float dispersion_std) {
        Vec3d pos = getPlayerFiringPoint(player);
        Vec3d front = Vec3d.fromPitchYaw(player.rotationPitch, player.rotationYaw);

        float angle = (float)Math.PI * 2 * random.nextFloat();
        float gaussian = Math.abs((float)random.nextGaussian());
        if (gaussian > 4) gaussian = 4;

        front = front.rotatePitch(dispersion_std * gaussian * MathHelper.sin(angle))
                       .rotateYaw(dispersion_std * gaussian * MathHelper.cos(angle));

        Vec3d motion = front.scale(BulletEntity.VELOCITY);

        Vec3d playerMotion = player.getMotion();
        motion.add(playerMotion.x, player.onGround ? 0 : playerMotion.y, playerMotion.z);

        BulletEntity bullet = new BulletEntity(worldIn);
        bullet.shooterUuid = player.getUniqueID();
        bullet.setPosition(pos.x, pos.y, pos.z);
        bullet.setMotion(motion);

        worldIn.addEntity(bullet);
    }

    private void fireParticles(World world, PlayerEntity player) {
        Vec3d pos = getPlayerFiringPoint(player);
        Vec3d front = Vec3d.fromPitchYaw(player.rotationPitch, player.rotationYaw);

        for (int i = 0; i != 10; ++i) {
            float t = random.nextFloat();

            Vec3d p = pos.add(front.scale(0.5 + t));
            Vec3d v = front.scale(0.1 + 0.05 * (1 - t));

            world.addParticle(ParticleTypes.SMOKE,
                p.x,
                p.y,
                p.z,
                v.x + (random.nextFloat() - 0.5) * (1 - t) * 0.05,
                v.y + (random.nextFloat() - 0.5) * (1 - t) * 0.05,
                v.z + (random.nextFloat() - 0.5) * (1 - t) * 0.05
            );
        }
    }

    private void setLoaded(ItemStack stack, boolean loaded) {
        stack.getOrCreateTag().putByte("loaded", (byte)(loaded ? 1 : 0));
    }

    private boolean isLoaded(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.getByte("loaded") == 1;
    }

    private void setReady(ItemStack stack, boolean ready) {
        stack.getOrCreateTag().putByte("ready", (byte)(ready ? 1 : 0));
    }

    private boolean isReady(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.getByte("ready") == 1;
    }
}
