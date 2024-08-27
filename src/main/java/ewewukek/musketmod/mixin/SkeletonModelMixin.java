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
        if (GunItem.isHoldingGun(mob) && mob.isUsingItem()) {
            SkeletonModel<?> model = (SkeletonModel<?>)(Object)this;
            if (mob.getMainArm() == HumanoidArm.RIGHT) {
                model.rightArmPose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            } else {
                model.leftArmPose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
        }
    }

    @Redirect(method = "setupAnim", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    boolean isBowOrGun(ItemStack stack, Item item) {
        return stack.is(item) || (stack.getItem() instanceof GunItem);
    }
}
