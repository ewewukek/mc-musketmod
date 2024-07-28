package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.ILootTableId;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(LootDataManager.class)
public class LootDataManagerMixin {
    @Inject(method = "getElement", at = @At("HEAD"))
    private void storeLocations(LootDataId<LootTable> id, CallbackInfoReturnable<LootTable> ci) {
        Object table = ((LootDataManager)(Object)this).elements.get(id);
        if (table instanceof LootTable) {
            ((ILootTableId)table).setLocation(id.location());
        }
    }
}
