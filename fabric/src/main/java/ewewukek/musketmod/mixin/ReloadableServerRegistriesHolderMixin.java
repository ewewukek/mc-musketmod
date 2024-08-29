package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.ILootTableId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(ReloadableServerRegistries.Holder.class)
abstract class ReloadableServerRegistriesHolderMixin {
    private ResourceLocation location;

    @Inject(method = "getLootTable", at = @At("HEAD"))
    private void getLootTableHead(ResourceKey<LootTable> key, CallbackInfoReturnable<LootTable> ci) {
        location = key.location();
    }

    @Inject(method = "getLootTable", at = @At("RETURN"))
    private void getLootTable(CallbackInfoReturnable<LootTable> ci) {
        ((ILootTableId)ci.getReturnValue()).setLocation(location);
    }
}
