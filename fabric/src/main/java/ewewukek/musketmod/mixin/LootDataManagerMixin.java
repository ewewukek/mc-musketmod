package ewewukek.musketmod.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.ILootTableId;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(LootDataManager.class)
abstract class LootDataManagerMixin {
    @Shadow
    private Map<LootDataId<?>, ?> elements;

    @Inject(method = "getElement", at = @At("HEAD"))
    private void getElement(LootDataId<LootTable> id, CallbackInfoReturnable<LootTable> ci) {
        Object table = elements.get(id);
        if (table instanceof LootTable) {
            ((ILootTableId)table).setLocation(id.location());
        }
    }
}
