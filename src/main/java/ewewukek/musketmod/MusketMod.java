package ewewukek.musketmod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("musketmod")
public class MusketMod {
    public MusketMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            Item cartridge = new Item(new Item.Properties().group(ItemGroup.COMBAT));
            cartridge.setRegistryName("musketmod", "cartridge");
            event.getRegistry().registerAll(cartridge);
        }
    }
}
