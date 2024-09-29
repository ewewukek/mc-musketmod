package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ewewukek.musketmod.ClientUtilities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.world.entity.LivingEntity;

@Mixin(HumanoidModel.class)
abstract class HumanoidModelMixin {
    private LivingEntity entity;
    private ArmPose leftArmPose;
    private ArmPose rightArmPose;

    @Inject(method = "setupAnim", at = @At("HEAD"))
    private void setupAnimHead(LivingEntity entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        this.entity = entity;
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        leftArmPose = model.leftArmPose;
        rightArmPose = model.rightArmPose;
    }

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    private void poseRightArm(CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        if (ClientUtilities.poseArm(entity, model, model.rightArm)) {
            model.rightArmPose = ArmPose.SPYGLASS; // to disable AnimationUtils.bobModelPart call
        }
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    private void poseLeftArm(CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        if (ClientUtilities.poseArm(entity, model, model.leftArm)) {
            model.leftArmPose = ArmPose.SPYGLASS; // to disable AnimationUtils.bobModelPart call
        }
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void setupAnimTail(CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        model.rightArmPose = rightArmPose;
        model.leftArmPose = leftArmPose;
    }
}
