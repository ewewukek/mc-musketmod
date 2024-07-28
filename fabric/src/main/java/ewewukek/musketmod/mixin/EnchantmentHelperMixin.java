package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import ewewukek.musketmod.VanillaHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    private static Enchantment enchantment;

    @Redirect(method = "getAvailableEnchantmentResults", at = @At(value = "FIELD",
        target = "Lnet/minecraft/world/item/enchantment/Enchantment;category:Lnet/minecraft/world/item/enchantment/EnchantmentCategory;"))
    private static EnchantmentCategory category(Enchantment enchantment) {
        EnchantmentHelperMixin.enchantment = enchantment;
        return enchantment.category;
    }

    @Redirect(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean canEnchant(EnchantmentCategory category, Item item) {
        return category.canEnchant(item)
            || VanillaHelper.canEnchant(enchantment, new ItemStack(item));
    }
}
