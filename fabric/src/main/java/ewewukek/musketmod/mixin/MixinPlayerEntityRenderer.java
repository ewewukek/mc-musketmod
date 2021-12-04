package ewewukek.musketmod.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.ClientUtilities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;

@Mixin(PlayerRenderer.class)
public class MixinPlayerEntityRenderer {
    @Inject(
        method = "getArmPose",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void getArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> ci) {
        Optional<HumanoidModel.ArmPose> armPose = ClientUtilities.getArmPose(player, hand);
        if (armPose.isPresent()) {
            ci.setReturnValue(armPose.get());
            ci.cancel();
        }
    }
}
