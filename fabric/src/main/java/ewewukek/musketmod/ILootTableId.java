package ewewukek.musketmod;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public interface ILootTableId {
    ResourceKey<LootTable> getKey();
    void setKey(ResourceKey<LootTable> key);
}
