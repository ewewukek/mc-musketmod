package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ewewukek.musketmod.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(SkeletonModel.class)
public class SkeletonModelMixin {
    private Mob mob;

    @ModifyVariable(method = "prepareMobModel", at = @At("HEAD"))
    Mob storeMob(Mob mob) {
        return (this.mob = mob);
    }

    @Inject(method = "prepareMobModel", at = @At("TAIL"))
    void prepareMobModel(CallbackInfo ci) {
        if (mob != null) {
            InteractionHand hand = GunItem.getHoldingHand(mob);
            if (hand != null) {
                ItemStack stack = mob.getItemInHand(hand);
                HumanoidModel.ArmPose pose = GunItem.isLoaded(stack)
                    ? HumanoidModel.ArmPose.CROSSBOW_HOLD
                    : HumanoidModel.ArmPose.EMPTY;
                SkeletonModel<?> model = (SkeletonModel<?>)(Object)this;
                HumanoidArm arm = hand == InteractionHand.MAIN_HAND
                    ? mob.getMainArm() : mob.getMainArm().getOpposite();
                if (arm == HumanoidArm.RIGHT) {
                    model.rightArmPose = pose;
                } else {
                    model.leftArmPose = pose;
                }
            }
        }
    }

    @Redirect(method = "setupAnim", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    boolean isBowOrGun(ItemStack stack, Item item) {
        return stack.is(item) || (stack.getItem() instanceof GunItem);
    }
}
