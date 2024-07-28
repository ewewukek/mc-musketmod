package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import ewewukek.musketmod.GunItem;
import ewewukek.musketmod.VanillaHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.extensions.IForgeItem;

@Mixin(GunItem.class)
public class GunItemMixin implements IForgeItem {
    @Override
    public boolean isRepairable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return VanillaHelper.canEnchant(enchantment, stack);
    }
}
