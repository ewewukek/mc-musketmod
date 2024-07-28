package ewewukek.musketmod;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("musketmod.txt");

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(
        resource("main"), () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public MusketMod() {
        Config.load();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::register);
        bus.addListener(this::creativeTabs);
        MinecraftForge.EVENT_BUS.addListener(this::worldTick);
        MinecraftForge.EVENT_BUS.addListener(this::reload);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> new ClientSetup(bus));

        NETWORK_CHANNEL.registerMessage(1, SmokeEffectPacket.class,
        SmokeEffectPacket::encode, SmokeEffectPacket::new, SmokeEffectPacket::handle);
    }

    public void register(final RegisterEvent event) {
        Items.register((path, item) -> {
            event.register(Registries.ITEM, resource(path), () -> item);
        });
        Sounds.register((sound) -> {
            event.register(Registries.SOUND_EVENT, sound.getLocation(), () -> sound);
        });
        event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
            BulletEntity.ENTITY_TYPE = EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .setTrackingRange(64).setUpdateInterval(20)
                .setShouldReceiveVelocityUpdates(false)
                .build("bullet");
            helper.register(resource("bullet"), BulletEntity.ENTITY_TYPE);
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
        NETWORK_CHANNEL.send(PacketDistributor.NEAR.with(() -> point),
            new SmokeEffectPacket(origin, direction));
    }

    public static class SmokeEffectPacket {
        public final Vec3 origin;
        public final Vec3 direction;

        public SmokeEffectPacket(Vec3 origin, Vec3 direction) {
            this.origin = origin;
            this.direction = direction;
        }

        public SmokeEffectPacket(FriendlyByteBuf buf) {
            origin = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
            direction = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        public void encode(FriendlyByteBuf buf) {
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
