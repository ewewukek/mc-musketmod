package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import ewewukek.musketmod.GunItem;
import ewewukek.musketmod.VanillaHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.extensions.IItemExtension;

@Mixin(GunItem.class)
abstract class GunItemMixin implements IItemExtension {
    @Override
    public boolean isRepairable(ItemStack stack) {
        return ((Item)(Object)this).isRepairable(stack);
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return VanillaHelper.canEnchant(enchantment, stack);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return VanillaHelper.canEnchant(enchantment, stack);
    }
}
