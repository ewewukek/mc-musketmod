package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ewewukek.musketmod.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;

@Mixin(SkeletonModel.class)
abstract class SkeletonModelMixin {
    private Mob mob;

    @Inject(method = "prepareMobModel", at = @At("HEAD"))
    private void prepareMobModelHead(Mob mob, float f, float g, float h, CallbackInfo ci) {
        this.mob = mob;
    }

    @Inject(method = "prepareMobModel", at = @At("TAIL"))
    private void prepareMobModelTail(CallbackInfo ci) {
        if (GunItem.isHoldingGun(mob) && mob.isUsingItem()) {
            SkeletonModel<?> model = (SkeletonModel<?>)(Object)this;
            if (mob.getMainArm() == HumanoidArm.RIGHT) {
                model.rightArmPose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            } else {
                model.leftArmPose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
        }
    }

    @Inject(method = "setupAnim", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
        target = "Lnet/minecraft/world/entity/Mob;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private void setupAnim(CallbackInfo ci) {
        if (mob != null && GunItem.isHoldingGun(mob)) {
            ci.cancel();
        }
    }
}
