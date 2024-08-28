package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ewewukek.musketmod.ClientUtilities;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    private LivingEntity entity;
    private boolean leftArmSet;
    private boolean rightArmSet;

    @ModifyVariable(method = "setupAnim", at = @At("HEAD"))
    private LivingEntity setupAnim(LivingEntity entity) {
        return (this.entity) = entity;
    }

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    private void poseRightArm(CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        rightArmSet = ClientUtilities.poseArm(entity, model.rightArm, model.head, true);
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    private void poseLeftArm(CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        leftArmSet = ClientUtilities.poseArm(entity, model.leftArm, model.head, false);
    }

    @Redirect(method = "setupAnim", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V"))
    private void bobModelPart(ModelPart arm, float amount, float sign) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        boolean armSet = arm == model.rightArm ? rightArmSet : leftArmSet;
        if (!armSet) {
            AnimationUtils.bobModelPart(arm, amount, sign);
        }
    }
}
