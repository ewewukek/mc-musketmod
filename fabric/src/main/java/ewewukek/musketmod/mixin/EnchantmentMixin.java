package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.VanillaHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(Enchantment.class)
abstract class EnchantmentMixin {
    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    private void canEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        if (VanillaHelper.canEnchant((Enchantment)(Object)this, stack)) {
            ci.setReturnValue(true);
            ci.cancel();
        }
    }
}
