package ewewukek.musketmod;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
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
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        });
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(
                    new Item(new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS)).setRegistryName(MODID, "barrel"),
                    new Item(new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS)).setRegistryName(MODID, "stock"),
                    new Item(new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)).setRegistryName(MODID, "cartridge"),
                    new MusketItem(new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)).setRegistryName(MODID, "musket")
            );
        }

        @SubscribeEvent
        public static void onEntityRegistry(final RegistryEvent.Register<EntityType<?>> event) {
            event.getRegistry().register(
                    EntityType.Builder.<BulletEntity>createNothing(MobCategory.MISC)
                            .setCustomClientFactory(BulletEntity::new).sized(0.5f, 0.5f)
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

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onAddReloadListenerEvent(final AddReloadListenerEvent event) {
            event.addListener(new PreparableReloadListener() {
                @Override
                public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager,
                    ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor,
                    Executor gameExecutor) {

                    return stage.wait(Unit.INSTANCE).thenRunAsync(() -> {
                        Config.reload();
                    }, gameExecutor);
                }
            });
        }
    }
}
