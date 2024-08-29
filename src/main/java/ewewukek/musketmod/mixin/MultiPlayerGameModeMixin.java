package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.ClientUtilities;
import ewewukek.musketmod.GunItem;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(MultiPlayerGameMode.class)
abstract class MultiPlayerGameModeMixin {
    @Inject(method = "interactAt", at = @At("RETURN"))
    private void interactAt(CallbackInfoReturnable<InteractionResult> ci) {
        if (ci.getReturnValue().consumesAction()) {
            ClientUtilities.preventFiring = true;
        }
    }

    @Inject(method = "interact", at = @At("RETURN"))
    private void interact(CallbackInfoReturnable<InteractionResult> ci) {
        if (ci.getReturnValue().consumesAction()) {
            ClientUtilities.preventFiring = true;
        }
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void useItemOn(CallbackInfoReturnable<InteractionResult> ci) {
        if (ci.getReturnValue().consumesAction()) {
            ClientUtilities.preventFiring = true;
        }
    }

    private boolean gunReady;

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void useItemHead(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        gunReady = false;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof GunItem gun && GunItem.isReady(stack)
        && gun.canUseFrom(player, hand)) {
            gunReady = true;
            if (ClientUtilities.preventFiring) {
                ci.setReturnValue(InteractionResult.FAIL);
                ci.cancel();
            }
        }
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    private void useItemTail(CallbackInfoReturnable<InteractionResult> ci) {
        if (!gunReady && ci.getReturnValue().consumesAction()) {
            ClientUtilities.preventFiring = true;
        }
    }
}
