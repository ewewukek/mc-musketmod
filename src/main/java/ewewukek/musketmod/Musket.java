package ewewukek.musketmod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class Musket extends Item {
    public Musket() {
        super(new Item.Properties()
            .defaultMaxDamage(250)
            .group(ItemGroup.COMBAT));
        setRegistryName(MusketMod.MODID, "musket");
    }
}
