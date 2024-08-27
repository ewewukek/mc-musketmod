package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ewewukek.musketmod.ClientUtilities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    private LivingEntity entity;

    @ModifyVariable(method = "poseRightArm", at = @At("HEAD"))
    public LivingEntity poseRightArm(LivingEntity entity) {
        return (this.entity = entity);
    }

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    public void poseRightArm(CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        ClientUtilities.poseArm(entity, model.rightArm, model.leftArm, model.head, true);
    }

    @ModifyVariable(method = "poseLeftArm", at = @At("HEAD"))
    public LivingEntity poseLeftArm(LivingEntity entity) {
        return (this.entity = entity);
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    public void poseLeftArm(CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        ClientUtilities.poseArm(entity, model.rightArm, model.leftArm, model.head, false);
    }
}
