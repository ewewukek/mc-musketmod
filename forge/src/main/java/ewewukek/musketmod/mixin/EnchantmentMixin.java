package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.IEnchantmentId;
import ewewukek.musketmod.VanillaHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(Enchantment.class)
abstract class EnchantmentMixin implements IEnchantmentId {
    private Holder<Enchantment> holder;

    @Override
    public Holder<Enchantment> getHolder() {
        return holder;
    }

    @Override
    public void setHolder(Holder<Enchantment> holder) {
        this.holder = holder;
    }

    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    private void canEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        if (holder != null && VanillaHelper.canEnchant(holder, stack)) {
            ci.setReturnValue(true);
            ci.cancel();
        }
    }
}
