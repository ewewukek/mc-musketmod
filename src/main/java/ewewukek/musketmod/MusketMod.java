package ewewukek.musketmod;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";

    public MusketMod() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(BulletEntity.class, BulletRenderer::new);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(
                new Item(new Item.Properties().group(ItemGroup.MISC)).setRegistryName(MODID, "barrel"),
                new Item(new Item.Properties().group(ItemGroup.MISC)).setRegistryName(MODID, "stock"),
                new Item(new Item.Properties().group(ItemGroup.COMBAT)).setRegistryName(MODID, "cartridge"),
                new MusketItem(new Item.Properties().group(ItemGroup.COMBAT)).setRegistryName(MODID, "musket")
            );
        }

        @SubscribeEvent
        public static void onEntityRegistry(final RegistryEvent.Register<EntityType<?>> event) {
            event.getRegistry().register(
                EntityType.Builder.<BulletEntity>create(EntityClassification.MISC)
                    .setCustomClientFactory(BulletEntity::new)
                    .setTrackingRange(64).setUpdateInterval(5).setShouldReceiveVelocityUpdates(false)
                    .build(MODID + ":bullet").setRegistryName(MODID, "bullet")
            );
        }

        @SubscribeEvent
        public static void onSoundRegistry(final RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().registerAll(
                new SoundEvent(new ResourceLocation(MODID, "musket_ready")).setRegistryName(MODID, "musket_ready"),
                new SoundEvent(new ResourceLocation(MODID, "musket_fire")).setRegistryName(MODID, "musket_fire")
            );
        }
    }
}
