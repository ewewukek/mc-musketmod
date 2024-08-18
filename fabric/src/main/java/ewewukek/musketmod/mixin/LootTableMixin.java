package ewewukek.musketmod.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import ewewukek.musketmod.ILootTableId;
import ewewukek.musketmod.VanillaHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(LootTable.class)
public class LootTableMixin implements ILootTableId {
    private ResourceKey<LootTable> key;

    @Override
    public ResourceKey<LootTable> getKey() {
        return key;
    }

    @Override
    public void setKey(ResourceKey<LootTable> key) {
        this.key = key;
    }

    @Redirect(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V"))
    void getRandomItems(LootTable table, LootContext context, Consumer<ItemStack> adder) {
        table.getRandomItems(context, adder);
        VanillaHelper.modifyLootTableItems(key.location(), context, adder);
    }
}
