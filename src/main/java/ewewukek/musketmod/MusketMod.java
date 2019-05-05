package ewewukek.musketmod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";

    public MusketMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(
                new Item(new Item.Properties().group(ItemGroup.COMBAT)).setRegistryName(MODID, "cartridge"),
                new Item(new Item.Properties().group(ItemGroup.COMBAT)).setRegistryName(MODID, "musket"),
                new Item(new Item.Properties().group(ItemGroup.MISC)).setRegistryName(MODID, "barrel"),
                new Item(new Item.Properties().group(ItemGroup.MISC)).setRegistryName(MODID, "stock")
            );
        }
    }
}
