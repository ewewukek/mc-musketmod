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
    public static final TagKey<Item> ENCHANTABLE_KNOCKBACK = TagKey.create(Registries.ITEM, MusketMod.resource("enchantable/knockback"));
    public static final TagKey<Item> ENCHANTABLE_POWER = TagKey.create(Registries.ITEM, MusketMod.resource("enchantable/power"));
    public static final TagKey<Item> ENCHANTABLE_SWORD_DAMAGE = TagKey.create(Registries.ITEM, MusketMod.resource("enchantable/sword_damage"));
    public static final TagKey<Item> ENCHANTABLE_FLAME = TagKey.create(Registries.ITEM, MusketMod.resource("enchantable/flame"));
    public static final TagKey<Item> ENCHANTABLE_QUICK_CHARGE = TagKey.create(Registries.ITEM, MusketMod.resource("enchantable/quick_charge"));

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
        if (enchantment.is(Enchantments.KNOCKBACK)) {
            return stack.is(ENCHANTABLE_KNOCKBACK);
        }
        if (enchantment.is(Enchantments.POWER)) {
            return stack.is(ENCHANTABLE_POWER);
        }
        if (enchantment.is(Enchantments.SHARPNESS)
        || enchantment.is(Enchantments.SMITE)
        || enchantment.is(Enchantments.BANE_OF_ARTHROPODS)) {
            return stack.is(ENCHANTABLE_SWORD_DAMAGE);
        }
        if (enchantment.is(Enchantments.FLAME)) {
            return stack.is(ENCHANTABLE_FLAME);
        }
        if (enchantment.is(Enchantments.QUICK_CHARGE)) {
            return stack.is(ENCHANTABLE_QUICK_CHARGE);
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
