package ewewukek.musketmod;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class Items {
    public static final Item MUSKET = new MusketItem(new Item.Properties()
        .durability(Config.musketDurability));
    public static final Item MUSKET_WITH_BAYONET = new MusketItem(new Item.Properties()
        .durability(Config.musketDurability)
        .attributes(MusketItem.createBayonetAttributes()));
    public static final Item MUSKET_WITH_SCOPE = new ScopedMusketItem(new Item.Properties()
        .durability(Config.scopedMusketDurability));
    public static final Item BLUNDERBUSS = new BlunderbussItem(new Item.Properties()
        .durability(Config.blunderbussDurability));
    public static final Item PISTOL = new PistolItem(new Item.Properties()
        .durability(Config.pistolDurability));
    public static final Item CARTRIDGE = new CartridgeItem(new Item.Properties());
    public static final Item MUSKET_UPGRADE = new MusketUpgradeItem();

    public static void registerDataComponentTypes(BiConsumer<String, DataComponentType<?>> helper) {
        helper.accept("loaded", GunItem.LOADED);
        helper.accept("loading_stage", GunItem.LOADING_STAGE);
    }

    public static void register(BiConsumer<String, Item> helper) {
        helper.accept("musket", MUSKET);
        helper.accept("musket_with_bayonet", MUSKET_WITH_BAYONET);
        helper.accept("musket_with_scope", MUSKET_WITH_SCOPE);
        helper.accept("blunderbuss", BLUNDERBUSS);
        helper.accept("pistol", PISTOL);
        helper.accept("cartridge", CARTRIDGE);
        helper.accept("musket_upgrade_smithing_template", MUSKET_UPGRADE);
    }

    public static void addToCreativeTab(ResourceKey<CreativeModeTab> tab, Consumer<Item> helper) {
        if (tab == CreativeModeTabs.COMBAT) {
            helper.accept(MUSKET);
            helper.accept(MUSKET_WITH_BAYONET);
            helper.accept(MUSKET_WITH_SCOPE);
            helper.accept(BLUNDERBUSS);
            helper.accept(PISTOL);
            helper.accept(CARTRIDGE);
        }
        if (tab == CreativeModeTabs.INGREDIENTS) {
            helper.accept(MUSKET_UPGRADE);
        }
    }
}
