package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ewewukek.musketmod.ScopedMusketItem;
import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Redirect(method = "aiStep", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean aiStepCheck(LocalPlayer player) {
        return player.isUsingItem() || ScopedMusketItem.isScoping;
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean canStartSprinting(LocalPlayer player) {
        return player.isUsingItem() || ScopedMusketItem.isScoping;
    }

    @Redirect(method = "getViewXRot", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
    public float getViewXRot(LocalPlayer player, float dt) {
        float xRot = player.getXRot();
        if (ScopedMusketItem.recoilTicks > 0) {
            xRot -= (ScopedMusketItem.recoilTicks - dt) * ScopedMusketItem.RECOIL_AMOUNT;
        }
        return xRot;
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    public void updateRecoil(CallbackInfo ci) {
        if (ScopedMusketItem.recoilTicks > 0) {
            ScopedMusketItem.recoilTicks--;
        }
    }
}
