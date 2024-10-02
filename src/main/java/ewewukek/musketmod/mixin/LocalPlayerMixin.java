package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.ScopedMusketItem;
import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    private boolean insideAiStep;

    @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true)
    private void canStartSprinting(CallbackInfoReturnable<Boolean> ci) {
        if (ScopedMusketItem.isScoping) {
            ci.setReturnValue(false);
            ci.cancel();
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void aiStepHead(CallbackInfo ci) {
        insideAiStep = true;
    }

    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void isUsingItem(CallbackInfoReturnable<Boolean> ci) {
        if (insideAiStep && ScopedMusketItem.isScoping) {
            ci.setReturnValue(true);
            ci.cancel();
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void aiStepTail(CallbackInfo ci) {
        insideAiStep = false;

        if (ScopedMusketItem.recoilTicks > 0) {
            ScopedMusketItem.recoilTicks--;
        }
    }

    @Inject(method = "getViewXRot", at = @At(value = "HEAD"), cancellable = true)
    private void getViewXRot(float dt, CallbackInfoReturnable<Float> ci) {
        if (ScopedMusketItem.isScoping && ScopedMusketItem.recoilTicks > 0) {
            LocalPlayer player = (LocalPlayer)(Object)this;
            float xRot = player.getXRot() - (ScopedMusketItem.recoilTicks - dt) * ScopedMusketItem.RECOIL_AMOUNT;
            ci.setReturnValue(xRot);
            ci.cancel();
        }
    }
}
