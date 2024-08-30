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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Shadow
    protected abstract void startUseItem();

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
        @SuppressWarnings("resource")
        Minecraft client = (Minecraft)(Object)this;
        Player player = client.player;
        ItemStack stack = player.getMainHandItem();

        boolean canUseScope = stack.getItem() == Items.MUSKET_WITH_SCOPE
            && GunItem.canUse(player)
            && client.options.getCameraType().isFirstPerson();

        if (player.isUsingItem()) {
            ItemStack usedStack = player.getUseItem();
            int delay = canUseScope ? 10 : 5;
            if (usedStack.getItem() instanceof GunItem && GunItem.isLoaded(usedStack)
            && player.getTicksUsingItem() >= GunItem.reloadDuration(usedStack) + delay) {

                ClientUtilities.preventFiring = true;
                client.gameMode.releaseUsingItem(player);
            }
        }

        boolean canContinueScoping = client.options.keyUse.isDown()
            && (GunItem.isReady(stack) || client.options.keyAttack.isDown());

        if (!canUseScope || !canContinueScoping) {
            ClientUtilities.setScoping(player, false);
        }

        ClientUtilities.canUseScope = canUseScope;
        ClientUtilities.attackKeyDown = client.options.keyAttack.isDown();
        if (!client.options.keyUse.isDown()) {
            ClientUtilities.preventFiring = false;
        }
    }
}
