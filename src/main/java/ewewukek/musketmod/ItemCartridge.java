package ewewukek.musketmod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class ItemCartridge extends Item {
    public ItemCartridge() {
        super(new Item.Properties().group(ItemGroup.COMBAT));
        setRegistryName(MusketMod.MODID, "cartridge");
    }
}
