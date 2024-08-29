package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.ILootTableId;
import ewewukek.musketmod.VanillaHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(LootTable.class)
abstract class LootTableMixin implements ILootTableId {
    private ResourceLocation location;
    private LootContext context;

    @Override
    public void setLocation(ResourceLocation location) {
        this.location = location;
    }

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
        at = @At("HEAD"))
    private void getRandomItemsHead(LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> ci) {
        this.context = context;
    }

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
        at = @At("RETURN"))
    private void getRandomItems(CallbackInfoReturnable<ObjectArrayList<ItemStack>> ci) {
        if (location != null) {
            ObjectArrayList<ItemStack> items = ci.getReturnValue();
            VanillaHelper.modifyLootTableItems(location, context, items::add);
        }
    }
}
