package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ewewukek.musketmod.GunItem;
import ewewukek.musketmod.Items;
import ewewukek.musketmod.ScopedMusketItem;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE",
    target = "Lnet/minecraft/client/Minecraft;startUseItem()V"))
    private void handleKeyUseDown(Minecraft client) {
        ItemStack stack = client.player.getMainHandItem();
        if (stack.getItem() == Items.MUSKET_WITH_SCOPE && GunItem.isReady(stack)) {
            setScoping(client, true);
        } else if (!ScopedMusketItem.isScoping) {
            client.startUseItem();
        }
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE",
    target = "Lnet/minecraft/client/Minecraft;startAttack()Z"))
    private boolean handleKeyAttack(Minecraft client) {
        ItemStack stack = client.player.getMainHandItem();
        if (stack.getItem() == Items.MUSKET_WITH_SCOPE && ScopedMusketItem.isScoping) {
            client.startUseItem();
        } else {
            return client.startAttack();
        }
        return true;
    }

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void handleKeyUseUp(CallbackInfo ci) {
        Minecraft client = (Minecraft)(Object)this;
        ItemStack stack = client.player.getMainHandItem();
        if (stack.getItem() != Items.MUSKET_WITH_SCOPE
        || !client.options.keyUse.isDown()
        || !GunItem.isReady(stack) && !client.options.keyAttack.isDown()) {
            setScoping(client, false);
        }
    }

    private static void setScoping(Minecraft client, boolean scoping) {
        if (scoping != ScopedMusketItem.isScoping) {
            client.player.playSound(
                scoping ? SoundEvents.SPYGLASS_USE : SoundEvents.SPYGLASS_STOP_USING,
                1.0f, 1.0f);
            ScopedMusketItem.isScoping = scoping;
        }
    }
}
