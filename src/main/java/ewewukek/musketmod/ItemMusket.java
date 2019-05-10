package ewewukek.musketmod;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemMusket extends Item {
    public ItemMusket() {
        super(new Item.Properties()
            .defaultMaxDamage(250)
            .group(ItemGroup.COMBAT));
        setRegistryName(MusketMod.MODID, "musket");
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }

    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (!(entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer)entityLiving;
        if (isLoaded(stack)) {
            System.out.println("PEW!");
            if (!worldIn.isRemote) fireBullet(worldIn, player);
            stack.damageItem(1, player);
            setLoaded(stack, false);
        } else {
            System.out.println("lock and load");
            setLoaded(stack, true);
        }
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public EnumAction getUseAction(ItemStack stack) {
        return isLoaded(stack) ? EnumAction.BOW : EnumAction.BLOCK;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    private void fireBullet(World worldIn, EntityPlayer player) {
        EntityBullet bullet = new EntityBullet(worldIn);
        bullet.setPosition(player.posX, player.posY, player.posZ);
        worldIn.spawnEntity(bullet);
    }

    private void setLoaded(ItemStack stack, boolean loaded) {
        stack.getOrCreateTag().setByte("loaded", (byte)(loaded ? 1 : 0));
    }

    private boolean isLoaded(ItemStack stack) {
        NBTTagCompound tag = stack.getTag();
        return tag != null && tag.getByte("loaded") == 1;
    }
}
