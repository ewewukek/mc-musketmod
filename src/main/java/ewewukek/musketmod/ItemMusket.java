package ewewukek.musketmod;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Particles;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemMusket extends Item {
    public static final int RELOAD_DURATION = 30;
    public static final int AIM_DURATION = 20;
    public static final float DISPERSION_MULTIPLIER = 3;
    public static final float DISPERSION_STD = 0.4f * (float)Math.PI / 180;

    public ItemMusket() {
        super(new Item.Properties()
            .defaultMaxDamage(250)
            .group(ItemGroup.COMBAT));
        setRegistryName(MusketMod.MODID, "musket");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (!(entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer)entityLiving;
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
            stack.damageItem(1, player);
            setReady(stack, false);
            setLoaded(stack, false);
        } else if (isLoaded(stack)) {
            setReady(stack, true);
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entityLiving, int timeLeft) {
        if (getUseDuration(stack) - timeLeft >= RELOAD_DURATION && !isReady(stack) && !isLoaded(stack)) {
            setLoaded(stack, true);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getUseAction(ItemStack stack) {
        if (isReady(stack)) {
            return EnumAction.BOW;
        } else {
            return isLoaded(stack) ? EnumAction.NONE : EnumAction.BLOCK;
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    private void fireBullet(World worldIn, EntityPlayer player, float dispersion_std) {
        EntityBullet bullet = new EntityBullet(worldIn);

        Vec3d side = Vec3d.fromPitchYaw(0, player.rotationYaw + 90);
        if (player.getActiveHand() == EnumHand.OFF_HAND) side = side.scale(-1);
        Vec3d down = Vec3d.fromPitchYaw(player.rotationPitch + 90, player.rotationYaw);
        Vec3d spawnPoint = side.add(down).scale(0.1);

        bullet.setPosition(
            player.posX + spawnPoint.x,
            player.posY + spawnPoint.y + player.getEyeHeight(),
            player.posZ + spawnPoint.z
        );

        Vec3d front = Vec3d.fromPitchYaw(player.rotationPitch, player.rotationYaw);

        float angle = (float)Math.PI * 2 * random.nextFloat();
        float gaussian = Math.abs((float)random.nextGaussian());
        if (gaussian > 4) gaussian = 4;

        front = front.rotatePitch(dispersion_std * gaussian * MathHelper.sin(angle))
                       .rotateYaw(dispersion_std * gaussian * MathHelper.cos(angle));

        bullet.motionX = front.x * EntityBullet.VELOCITY;
        bullet.motionY = front.y * EntityBullet.VELOCITY;
        bullet.motionZ = front.z * EntityBullet.VELOCITY;

        bullet.motionX += player.motionX;
        bullet.motionZ += player.motionZ;
        if (!player.onGround) {
            bullet.motionY += player.motionY;
        }

        worldIn.spawnEntity(bullet);
    }

    private void fireParticles(World world, EntityPlayer player) {
        Vec3d front = Vec3d.fromPitchYaw(player.rotationPitch, player.rotationYaw);
        Vec3d side = Vec3d.fromPitchYaw(0, player.rotationYaw + 90);
        if (player.getActiveHand() == EnumHand.OFF_HAND) side = side.scale(-1);
        Vec3d down = Vec3d.fromPitchYaw(player.rotationPitch + 90, player.rotationYaw);

        Vec3d spawnPoint = new Vec3d(
            player.posX,
            player.posY + player.getEyeHeight(),
            player.posZ
        ).add(side.add(down).scale(0.1));

        for (int i = 0; i != 10; ++i) {
            float t = random.nextFloat();

            Vec3d p = spawnPoint.add(front.scale(0.5 + t));
            Vec3d v = front.scale(0.1 + 0.05 * (1 - t));

            world.spawnParticle(Particles.SMOKE,
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
        stack.getOrCreateTag().setByte("loaded", (byte)(loaded ? 1 : 0));
    }

    private boolean isLoaded(ItemStack stack) {
        NBTTagCompound tag = stack.getTag();
        return tag != null && tag.getByte("loaded") == 1;
    }

    private void setReady(ItemStack stack, boolean ready) {
        stack.getOrCreateTag().setByte("ready", (byte)(ready ? 1 : 0));
    }

    private boolean isReady(ItemStack stack) {
        NBTTagCompound tag = stack.getTag();
        return tag != null && tag.getByte("ready") == 1;
    }
}
