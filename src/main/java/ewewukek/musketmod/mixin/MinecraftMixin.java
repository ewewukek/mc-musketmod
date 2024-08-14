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
    @SuppressWarnings("resource")
    @Redirect(method = "startUseItem", at = @At(value = "INVOKE",
    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult handleKeyUseDown(MultiPlayerGameMode gameMode, Player player, InteractionHand hand) {
        Minecraft client = (Minecraft)(Object)this;
        ItemStack stack = player.getItemInHand(hand);
        if (client.options.getCameraType().isFirstPerson()
        && stack.getItem() == Items.MUSKET_WITH_SCOPE && GunItem.isReady(stack)) {
            setScoping(client, true);
            if (client.options.keyAttack.isDown()) {
                return gameMode.useItem(player, hand);
            }
        } else if (!ScopedMusketItem.isScoping) {
            return gameMode.useItem(player, hand);
        }
        return InteractionResult.FAIL;
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE",
    target = "Lnet/minecraft/client/Minecraft;startAttack()Z"))
    private boolean handleKeyAttack(Minecraft client) {
        ItemStack stack = client.player.getMainHandItem();
        if (stack.getItem() == Items.MUSKET_WITH_SCOPE && ScopedMusketItem.isScoping) {
            client.startUseItem();
            ScopedMusketItem.recoilTicks = ScopedMusketItem.RECOIL_TICKS;
        } else {
            return client.startAttack();
        }
        return true;
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void finishReloading(CallbackInfo ci) {
        @SuppressWarnings("resource")
        Minecraft client = (Minecraft)(Object)this;
        if (client.options.getCameraType().isFirstPerson() && client.player.isUsingItem()) {
            ItemStack stack = client.player.getUseItem();
            if (stack.getItem() == Items.MUSKET_WITH_SCOPE && GunItem.isLoaded(stack)
            && client.player.getTicksUsingItem() >= GunItem.RELOAD_DURATION + 10) {
                client.gameMode.releaseUsingItem(client.player);
            }
        }
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
        if (!scoping) ScopedMusketItem.recoilTicks = 0;
    }
}
