package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.ClientUtilities;
import ewewukek.musketmod.GunItem;
import ewewukek.musketmod.Items;
import ewewukek.musketmod.ScopedMusketItem;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Shadow
    protected abstract void startUseItem();

    private static boolean canUseScope(Minecraft client, Player player, ItemStack stack) {
        return stack.getItem() == Items.MUSKET_WITH_SCOPE
            && GunItem.canUse(player)
            && client.options.getCameraType().isFirstPerson();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void startUseItemInject(CallbackInfo ci) {
        Minecraft client = (Minecraft)(Object)this;
        ItemStack stack = client.player.getMainHandItem();

        if (stack.getItem() instanceof GunItem && GunItem.isReady(stack)
        && canUseScope(client, client.player, stack)) {

            setScoping(client, true);
            if (client.options.keyAttack.isDown()) {
                ClientUtilities.preventFiring = false;
                return;
            }
        }
        if (ScopedMusketItem.isScoping) {
            ci.cancel();
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void startAttack(CallbackInfoReturnable<Boolean> ci) {
        if (ScopedMusketItem.isScoping) {
            startUseItem();
            ci.cancel();
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void continueAttack(CallbackInfo ci) {
        if (ScopedMusketItem.isScoping) {
            ci.cancel();
        }
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void handleKeybinds(CallbackInfo ci) {
        Minecraft client = (Minecraft)(Object)this;
        Player player = client.player;

        if (player.isUsingItem()) {
            ItemStack stack = player.getUseItem();
            int delay = canUseScope(client, player, stack) ? 10 : 5;
            if (stack.getItem() instanceof GunItem && GunItem.isLoaded(stack)
            && player.getTicksUsingItem() >= GunItem.reloadDuration(stack) + delay) {

                client.gameMode.releaseUsingItem(player);
            }
        }

        ItemStack stack = player.getMainHandItem();
        boolean canContinueScoping = client.options.keyUse.isDown()
            && (GunItem.isReady(stack) || client.options.keyAttack.isDown());

        if (!canUseScope(client, player, stack) || !canContinueScoping) {
            setScoping(client, false);
        }

        if (!client.options.keyUse.isDown()) {
            ClientUtilities.preventFiring = false;
        }
    }

    private static void setScoping(Minecraft client, boolean scoping) {
        if (scoping != ScopedMusketItem.isScoping) {
            client.player.playSound(
                scoping ? SoundEvents.SPYGLASS_USE : SoundEvents.SPYGLASS_STOP_USING,
                1.0f, 1.0f);
            ScopedMusketItem.isScoping = scoping;
        }
        if (!scoping) ScopedMusketItem.recoilTicks = 0;
    }
}
