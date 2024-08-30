package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.ILootTableId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(LootDataManager.class)
abstract class LootDataManagerMixin {
    private ResourceLocation location;

    @Inject(method = "getElement", at = @At("HEAD"))
    private void getElementHead(LootDataId<LootTable> id, CallbackInfoReturnable<LootTable> ci) {
        location = id.location();
    }

    @Inject(method = "getElement", at = @At("RETURN"))
    private void getElement(CallbackInfoReturnable<LootTable> ci) {
        LootTable table = ci.getReturnValue();
        if (table != null) {
            ((ILootTableId)table).setLocation(location);
        }
    }
}
