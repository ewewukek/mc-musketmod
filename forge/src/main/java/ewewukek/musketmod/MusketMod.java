package ewewukek.musketmod;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("musketmod.txt");

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private static final int PROTOCOL_VERSION = 2;
    public static final SimpleChannel NETWORK_CHANNEL = ChannelBuilder.named(
        resource("main"))
        .networkProtocolVersion(PROTOCOL_VERSION)
        .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
        .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
        .simpleChannel();

    public MusketMod() {
        Config.load();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::register);
        bus.addListener(this::creativeTabs);
        MinecraftForge.EVENT_BUS.addListener(this::worldTick);
        MinecraftForge.EVENT_BUS.addListener(this::reload);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> new ClientSetup(bus));

        NETWORK_CHANNEL.messageBuilder(SmokeEffectPacketWrapper.class)
            .encoder(SmokeEffectPacketWrapper::encode)
            .decoder(SmokeEffectPacketWrapper::decode)
            .consumerNetworkThread(SmokeEffectPacketWrapper::handle)
            .add();
    }

    public void register(final RegisterEvent event) {
        Items.registerDataComponentTypes((path, component) -> {
            event.register(Registries.DATA_COMPONENT_TYPE, resource(path), () -> component);
        });
        Items.register((path, item) -> {
            event.register(Registries.ITEM, resource(path), () -> item);
        });
        Sounds.register((sound) -> {
            event.register(Registries.SOUND_EVENT, sound.getLocation(), () -> sound);
        });
        event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
            BulletEntity.register((string, entityType) -> {
                helper.register(resource(string), entityType);
            });
        });
        event.register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
            resource("loot_modifier"), () -> ModLootModifier.CODEC);
    }

    public void creativeTabs(final BuildCreativeModeTabContentsEvent event) {
        Items.addToCreativeTab(event.getTabKey(), (item) -> {
            event.accept(item);
        });
    }

    public void worldTick(final TickEvent.LevelTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.LevelTickEvent.Phase.END) {
            DeferredDamage.apply();
        }
    }

    public void reload(final AddReloadListenerEvent event) {
        event.addListener(new PreparableReloadListener() {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager,
                ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor,
                Executor gameExecutor) {

                return stage.wait(Unit.INSTANCE).thenRunAsync(() -> {
                    Config.load();
                }, gameExecutor);
            }
        });
    }

    public static void disableVelocityUpdate(EntityType.Builder<?> builder) {
        builder.setShouldReceiveVelocityUpdates(false);
    }

    public static void sendSmokeEffect(ServerLevel level, Vec3 origin, Vec3 direction) {
        PacketDistributor.TargetPoint point = new PacketDistributor.TargetPoint(
            origin.x, origin.y, origin.z,
            64.0, level.dimension());
        NETWORK_CHANNEL.send(new SmokeEffectPacketWrapper(SmokeEffectPacket.fromVec3(origin, direction)),
            PacketDistributor.NEAR.with(point));
    }

    // TODO: use SmokeEffectPacket directly when forge adds api for CustomPacketPayload
    public static class SmokeEffectPacketWrapper {
        public final SmokeEffectPacket packet;

        public SmokeEffectPacketWrapper(SmokeEffectPacket packet) {
            this.packet = packet;
        }

        public static SmokeEffectPacketWrapper decode(FriendlyByteBuf buf) {
            return new SmokeEffectPacketWrapper(SmokeEffectPacket.CODEC.decode(buf));
        }

        public static void encode(SmokeEffectPacketWrapper msg, FriendlyByteBuf buf) {
            SmokeEffectPacket.CODEC.encode(buf, msg.packet);
        }

        public static void handle(SmokeEffectPacketWrapper msg, CustomPayloadEvent.Context context) {
            context.enqueueWork(() -> {
                ClientUtilities.handleSmokeEffectPacket(msg.packet);

            });
            context.setPacketHandled(true);
        }
    }
}
