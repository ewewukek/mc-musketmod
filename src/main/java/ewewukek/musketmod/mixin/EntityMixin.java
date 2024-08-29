package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.GunItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(Entity.class)
abstract class EntityMixin {
    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void spawnAtLocation(ItemStack stack, CallbackInfoReturnable<ItemEntity> ci) {
        Object object = this;
        if (stack.getItem() == Items.ARROW
        && (object instanceof AbstractSkeleton entity) && GunItem.isHoldingGun(entity)) {
            ItemStack cartridges = new ItemStack(ewewukek.musketmod.Items.CARTRIDGE, stack.getCount());
            ci.setReturnValue(entity.spawnAtLocation(cartridges, 0.0F));
            ci.cancel();
        }
    }
}
