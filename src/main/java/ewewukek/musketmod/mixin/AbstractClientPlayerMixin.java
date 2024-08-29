package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.Config;
import ewewukek.musketmod.ScopedMusketItem;
import net.minecraft.client.player.AbstractClientPlayer;

@Mixin(AbstractClientPlayer.class)
abstract class AbstractClientPlayerMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
    private void getFieldOfViewModifier(CallbackInfoReturnable<Float> ci) {
        if (ScopedMusketItem.isScoping) {
            ci.setReturnValue(1.0f / Config.scopeZoom);
            ci.cancel();
        }
    }
}
