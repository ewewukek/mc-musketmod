package ewewukek.musketmod;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class Items {
    public static final Item CARTRIDGE = new Item(new Item.Properties().tab(CreativeModeTab.TAB_COMBAT));
    public static final Item MUSKET = new MusketItem(new Item.Properties().tab(CreativeModeTab.TAB_COMBAT), false);
    public static final Item MUSKET_WITH_BAYONET = new MusketItem(new Item.Properties().tab(CreativeModeTab.TAB_COMBAT), true);
    public static final Item PISTOL = new PistolItem(new Item.Properties().tab(CreativeModeTab.TAB_COMBAT));
}
