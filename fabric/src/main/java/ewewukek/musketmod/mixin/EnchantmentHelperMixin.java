package ewewukek.musketmod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.VanillaHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {
    private static int cost;
    private static ItemStack stack;
    private static boolean treasure;

    @Inject(method = "getAvailableEnchantmentResults", at = @At(value = "HEAD"))
    private static void getAvailableEnchantmentResultsHead(int cost, ItemStack stack, boolean treasure, CallbackInfoReturnable<List<EnchantmentInstance>> ci) {
        EnchantmentHelperMixin.cost = cost;
        EnchantmentHelperMixin.stack = stack;
        EnchantmentHelperMixin.treasure = treasure;
    }

    @Inject(method = "getAvailableEnchantmentResults", at = @At(value = "RETURN"))
    private static void getAvailableEnchantmentResults(CallbackInfoReturnable<List<EnchantmentInstance>> ci) {
        if (stack.is(Items.BOOK)) {
            return;
        }
        List<EnchantmentInstance> list = ci.getReturnValue();
        for (Enchantment enchantment: BuiltInRegistries.ENCHANTMENT) {
            if (enchantment.category.canEnchant(stack.getItem())
            || (enchantment.isTreasureOnly() && !treasure) || !enchantment.isDiscoverable()
            || !VanillaHelper.canEnchant(enchantment, stack)) {
                continue;
            }
            for (int level = enchantment.getMaxLevel(); level >= enchantment.getMinLevel(); level--) {
                if (cost >= enchantment.getMinCost(level)
                && cost <= enchantment.getMaxCost(level)) {
                    list.add(new EnchantmentInstance(enchantment, level));
                }
            }
        }
    }
}
