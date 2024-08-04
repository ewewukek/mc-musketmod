package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.Config;
import ewewukek.musketmod.GunItem;
import ewewukek.musketmod.Items;
import ewewukek.musketmod.RangedGunAttackGoal;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;

@Mixin(Pillager.class)
public class PillagerMixin {
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void registerGoals(CallbackInfo ci) {
        Pillager pillager = (Pillager)(Object)this;
        pillager.goalSelector.addGoal(3, new RangedGunAttackGoal<>(pillager, 1.0, 8.0F));
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"), cancellable = true)
    private void populateDefaultEquipmentSlots(CallbackInfo ci) {
        Pillager pillager = (Pillager)(Object)this;
        if (pillager.getRandom().nextFloat() < Config.pistolPillagerChance) {
            pillager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.PISTOL));
            ci.cancel();
        }
    }

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private void getArmPose(CallbackInfoReturnable<AbstractIllager.IllagerArmPose> ci) {
        Pillager pillager = (Pillager)(Object)this;
        if (GunItem.isHoldingGun(pillager)) {
            ItemStack stack = pillager.getItemInHand(GunItem.getHoldingHand(pillager));
            if (GunItem.isLoaded(stack)) {
                ci.setReturnValue(AbstractIllager.IllagerArmPose.CROSSBOW_HOLD);
                ci.cancel();
            }
        }
    }
}
