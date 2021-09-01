package ewewukek.musketmod;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";

    @ObjectHolder(MusketMod.MODID + ":musket")
    public static Item MUSKET;
    @ObjectHolder(MusketMod.MODID + ":musket_with_bayonet")
    public static Item MUSKET_WITH_BAYONET;
    @ObjectHolder(MusketMod.MODID + ":pistol")
    public static Item PISTOL;
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
    @ObjectHolder(MusketMod.MODID + ":pistol_fire")
    public static SoundEvent SOUND_PISTOL_FIRE;

    @ObjectHolder(MusketMod.MODID + ":bullet")
    public static EntityType<BulletEntity> BULLET_ENTITY_TYPE;

    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MODID, "main"), () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public MusketMod() {
        Config.reload();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        });
        NETWORK_CHANNEL.registerMessage(1, SmokeEffectPacket.class,
            SmokeEffectPacket::encode, SmokeEffectPacket::new, SmokeEffectPacket::handle);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            Item stock = new Item(new Item.Properties().group(ItemGroup.MISC)) {
                @Override
                public int getBurnTime(ItemStack itemStack) {
                    return 200;
                }
            };
            event.getRegistry().registerAll(
                    new Item(new Item.Properties().group(ItemGroup.MISC)).setRegistryName(MODID, "barrel"),
                    stock.setRegistryName(MODID, "stock"),
                    new MusketItem(new Item.Properties().group(ItemGroup.COMBAT), false).setRegistryName(MODID, "musket"),
                    new MusketItem(new Item.Properties().group(ItemGroup.COMBAT), true).setRegistryName(MODID, "musket_with_bayonet"),
                    new PistolItem(new Item.Properties().group(ItemGroup.COMBAT)).setRegistryName(MODID, "pistol"),
                    new Item(new Item.Properties().group(ItemGroup.COMBAT)).setRegistryName(MODID, "cartridge")
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
                    new SoundEvent(new ResourceLocation(MODID, "musket_fire")).setRegistryName(MODID, "musket_fire"),
                    new SoundEvent(new ResourceLocation(MODID, "pistol_fire")).setRegistryName(MODID, "pistol_fire")
            );
        }

    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onAddReloadListenerEvent(final AddReloadListenerEvent event) {
            event.addListener(new IFutureReloadListener() {
                @Override
                public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager,
                    IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor,
                    Executor gameExecutor) {

                    return stage.markCompleteAwaitingOthers(Unit.INSTANCE).thenRunAsync(() -> {
                        Config.reload();
                    }, gameExecutor);
                }
            });
        }
    }

    public static void sendSmokeEffect(LivingEntity player, Vector3d origin, Vector3d direction) {
        NETWORK_CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
            new SmokeEffectPacket(origin, direction));
    }

    public static class SmokeEffectPacket {
        public final Vector3d origin;
        public final Vector3d direction;

        public SmokeEffectPacket(Vector3d origin, Vector3d direction) {
            this.origin = origin;
            this.direction = direction;
        }

        public SmokeEffectPacket(PacketBuffer buf) {
            origin = new Vector3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
            direction = new Vector3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        public void encode(PacketBuffer buf) {
            buf.writeFloat((float)origin.x);
            buf.writeFloat((float)origin.y);
            buf.writeFloat((float)origin.z);
            buf.writeFloat((float)direction.x);
            buf.writeFloat((float)direction.y);
            buf.writeFloat((float)direction.z);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSetup.handleSmokeEffectPacket(this, ctx));
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
