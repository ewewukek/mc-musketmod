package ewewukek.musketmod;

import net.minecraft.world.item.Item;

public class Items {
    public static final Item CARTRIDGE = new Item(new Item.Properties());
    public static final Item MUSKET = new MusketItem(new Item.Properties(), false);
    public static final Item MUSKET_WITH_BAYONET = new MusketItem(new Item.Properties(), true);
    public static final Item PISTOL = new PistolItem(new Item.Properties());
}
