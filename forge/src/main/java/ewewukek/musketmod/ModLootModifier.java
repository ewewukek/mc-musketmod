package ewewukek.musketmod;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class ModLootModifier extends LootModifier {
    public static final MapCodec<ModLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
        codecStart(instance)
        .apply(instance, ModLootModifier::new)
    );

    public ModLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    @NotNull
    public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> items, LootContext context) {
        ResourceLocation location = context.getQueriedLootTableId();
        if (location.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                MusketMod.resource(location.getPath()));
            context.getResolver().get(Registries.LOOT_TABLE, key).ifPresent(table -> {
                table.value().getRandomItemsRaw(context,
                    LootTable.createStackSplitter(context.getLevel(), items::add));
            });
        }
        return items;
    }
}
