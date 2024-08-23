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
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    private static boolean lockUseKey;

    private boolean canUseScope(Minecraft client, Player player, ItemStack stack) {
        return stack.getItem() == Items.MUSKET_WITH_SCOPE
            && GunItem.canUse(player)
            && client.options.getCameraType().isFirstPerson();
    }

    private boolean gunIsReady(Player player, InteractionHand hand, ItemStack stack) {
        return stack.getItem() instanceof GunItem gun
            && GunItem.isReady(stack) && gun.canUseFrom(player, hand);
    }

    @Redirect(method = "startUseItem", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult onUseItem(MultiPlayerGameMode gameMode, Player player, InteractionHand hand) {
        Minecraft client = (Minecraft)(Object)this;
        ItemStack stack = player.getItemInHand(hand);
        boolean bothGunsLoaded = false;

        if (gunIsReady(player, hand, stack)) {
            if (canUseScope(client, player, stack)) {
                lockUseKey = true;
                setScoping(client, true);
                if (client.options.keyAttack.isDown()) {
                    ScopedMusketItem.recoilTicks = ScopedMusketItem.RECOIL_TICKS;
                    return gameMode.useItem(player, hand);
                }
            }
            if (lockUseKey) return InteractionResult.FAIL;

            InteractionHand hand2 = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack stack2 = player.getItemInHand(hand2);

            bothGunsLoaded = gunIsReady(player, hand2, stack2);
        }
        if (ScopedMusketItem.isScoping) return InteractionResult.FAIL;

        InteractionResult result = gameMode.useItem(player, hand);
        if (result.consumesAction() && !bothGunsLoaded) {
            lockUseKey = true;
        }
        return result;
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/Minecraft;startAttack()Z"))
    private boolean handleKeyAttack(Minecraft client) {
        if (ScopedMusketItem.isScoping) {
            client.startUseItem();
            return true;
        }
        return client.startAttack();
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"))
    private void continueAttack(Minecraft client, boolean missed) {
        if (ScopedMusketItem.isScoping) return;
        client.continueAttack(missed);
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
            lockUseKey = false;
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
