package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import ewewukek.musketmod.GunItem;
import ewewukek.musketmod.IEnchantmentId;
import ewewukek.musketmod.VanillaHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.extensions.IForgeItem;

@Mixin(GunItem.class)
abstract class GunItemMixin implements IForgeItem {
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        Holder<Enchantment> holder = ((IEnchantmentId)(Object)enchantment).getHolder();
        return holder != null && VanillaHelper.canEnchant(holder, stack)
            || enchantment.isPrimaryItem(stack);
    }
}
