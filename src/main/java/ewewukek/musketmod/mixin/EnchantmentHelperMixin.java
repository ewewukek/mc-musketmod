package ewewukek.musketmod.mixin;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import ewewukek.musketmod.GunItem;
import ewewukek.musketmod.VanillaHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

//for Forge/NeoForge

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    private static ItemStack enchantedStack;

    @ModifyVariable(method = "getAvailableEnchantmentResults", at = @At("HEAD"))
    private static ItemStack storeStack(ItemStack stack) {
        return (enchantedStack = stack);
    }

    @Redirect(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE",
        target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private static Stream<Holder<Enchantment>> filterEnchantments(Stream<Holder<Enchantment>> stream, Predicate<Holder<Enchantment>> condition) {
        if (enchantedStack != null && enchantedStack.getItem() instanceof GunItem) {
            condition = condition.or(enchantment -> VanillaHelper.isPrimaryEnchantmentFor(enchantment, enchantedStack));
        }
        return stream.filter(condition);
    }
}
