package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
}
