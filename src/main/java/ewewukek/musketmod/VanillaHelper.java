package ewewukek.musketmod;

import java.util.List;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class VanillaHelper {
    public static void modifyLootTableItems(ResourceLocation location, LootContext context, Consumer<ItemStack> adder) {
        if (location.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                MusketMod.resource(location.getPath()));
            context.getResolver().get(Registries.LOOT_TABLE, key).ifPresent(modTable -> {
                modTable.value().getRandomItemsRaw(context,
                    LootTable.createStackSplitter(context.getLevel(), adder));
            });
        }
    }

    public static boolean canEnchant(Holder<Enchantment> enchantment, ItemStack stack) {
        if (stack.getItem() instanceof GunItem && enchantment.kind() == Holder.Kind.REFERENCE) {
            ResourceKey<Enchantment> key = enchantment.unwrapKey().get();
            if (key.location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                String tagPath = "enchantable/" + key.location().getPath();
                TagKey<Item> tag = TagKey.create(Registries.ITEM, MusketMod.resource(tagPath));
                return stack.is(tag);
            }
        }
        return false;
    }

    public static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            if (entry.getKey().is(enchantment)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    public static ItemStack getRandomWeapon(Entity entity, ResourceKey<LootTable> key) {
        LootTable table = entity.level().getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder((ServerLevel)entity.level())
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.ORIGIN, entity.position())
            .create(LootContextParamSets.SELECTOR);
        List<ItemStack> items = table.getRandomItems(params);
        if (items.size() > 0) {
            return items.get(0);
        } else {
            return ItemStack.EMPTY;
        }
    }
}
