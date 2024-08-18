package ewewukek.musketmod;

import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

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

    public static boolean isPrimaryEnchantmentFor(Holder<Enchantment> enchantment, ItemStack stack) {
        if (enchantment.kind() == Holder.Kind.REFERENCE) {
            ResourceKey<Enchantment> key = enchantment.unwrapKey().get();
            if (key.location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                String tagPath = "enchantable/" + key.location().getPath();
                TagKey<Item> tag = TagKey.create(Registries.ITEM, MusketMod.resource(tagPath));
                return stack.is(tag);
            }
        }
        return false;
    }

    public static boolean isAcceptableEnchantmentFor(Holder<Enchantment> enchantment, ItemStack stack) {
        if (enchantment.is(Enchantments.KNOCKBACK)) {
            return (stack.getItem() instanceof GunItem gun) && gun.twoHanded();
        }
        return isPrimaryEnchantmentFor(enchantment, stack);
    }

}
