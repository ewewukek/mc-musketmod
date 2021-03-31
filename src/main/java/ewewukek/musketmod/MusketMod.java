package ewewukek.musketmod;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";

    @ObjectHolder(MusketMod.MODID + ":musket")
    public static Item MUSKET;
    @ObjectHolder(MusketMod.MODID + ":cartridge")
    public static Item CARTRIDGE;

    @ObjectHolder(MusketMod.MODID + ":musket_load0")
    public static SoundEvent SOUND_MUSKET_LOAD_0;
    @ObjectHolder(MusketMod.MODID + ":musket_load1")
    public static SoundEvent SOUND_MUSKET_LOAD_1;
    @ObjectHolder(MusketMod.MODID + ":musket_load2")
    public static SoundEvent SOUND_MUSKET_LOAD_2;
    @ObjectHolder(MusketMod.MODID + ":musket_ready")
    public static SoundEvent SOUND_MUSKET_READY;
    @ObjectHolder(MusketMod.MODID + ":musket_fire")
    public static SoundEvent SOUND_MUSKET_FIRE;

    @ObjectHolder(MusketMod.MODID + ":bullet")
    public static EntityType<BulletEntity> BULLET_ENTITY_TYPE;

    public MusketMod() {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, Config.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
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
                            .setCustomClientFactory(BulletEntity::new).size(0.5f, 0.5f)
                            .setTrackingRange(64).setUpdateInterval(5).setShouldReceiveVelocityUpdates(false)
                            .build(MODID + ":bullet").setRegistryName(MODID, "bullet")
            );
        }

        @SubscribeEvent
        public static void onSoundRegistry(final RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().registerAll(
                    new SoundEvent(new ResourceLocation(MODID, "musket_load0")).setRegistryName(MODID, "musket_load0"),
                    new SoundEvent(new ResourceLocation(MODID, "musket_load1")).setRegistryName(MODID, "musket_load1"),
                    new SoundEvent(new ResourceLocation(MODID, "musket_load2")).setRegistryName(MODID, "musket_load2"),
                    new SoundEvent(new ResourceLocation(MODID, "musket_ready")).setRegistryName(MODID, "musket_ready"),
                    new SoundEvent(new ResourceLocation(MODID, "musket_fire")).setRegistryName(MODID, "musket_fire")
            );
        }

    }
}
