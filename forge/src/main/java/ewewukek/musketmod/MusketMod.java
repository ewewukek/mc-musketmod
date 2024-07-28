package ewewukek.musketmod;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("musketmod.txt");

    public static EntityType<BulletEntity> BULLET_ENTITY_TYPE;

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    private static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, MODID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(Registries.SOUND_EVENT, MODID);

    private static final int PROTOCOL_VERSION = 2;
    public static final SimpleChannel NETWORK_CHANNEL = ChannelBuilder.named(
        new ResourceLocation(MODID, "main"))
        .networkProtocolVersion(PROTOCOL_VERSION)
        .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
        .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
        .simpleChannel();

    public MusketMod() {
        Config.reload();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        DATA_COMPONENT_TYPES.register("loaded", () -> GunItem.LOADED);
        DATA_COMPONENT_TYPES.register("loading_stage", () -> GunItem.LOADING_STAGE);
        DATA_COMPONENT_TYPES.register(bus);

        ITEMS.register("musket", () -> Items.MUSKET);
        ITEMS.register("musket_with_bayonet", () -> Items.MUSKET_WITH_BAYONET);
        ITEMS.register("pistol", () -> Items.PISTOL);
        ITEMS.register("cartridge", () -> Items.CARTRIDGE);
        ITEMS.register(bus);

        SOUND_EVENTS.register(Sounds.MUSKET_LOAD_0.getLocation().getPath(), () -> Sounds.MUSKET_LOAD_0);
        SOUND_EVENTS.register(Sounds.MUSKET_LOAD_1.getLocation().getPath(), () -> Sounds.MUSKET_LOAD_1);
        SOUND_EVENTS.register(Sounds.MUSKET_LOAD_2.getLocation().getPath(), () -> Sounds.MUSKET_LOAD_2);
        SOUND_EVENTS.register(Sounds.MUSKET_READY.getLocation().getPath(), () -> Sounds.MUSKET_READY);
        SOUND_EVENTS.register(Sounds.MUSKET_FIRE.getLocation().getPath(), () -> Sounds.MUSKET_FIRE);
        SOUND_EVENTS.register(Sounds.PISTOL_FIRE.getLocation().getPath(), () -> Sounds.PISTOL_FIRE);
        SOUND_EVENTS.register(bus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> new ClientSetup(bus));

        bus.addListener(this::register);
        bus.addListener(this::creativeTabs);
        MinecraftForge.EVENT_BUS.addListener(this::reload);

        NETWORK_CHANNEL.messageBuilder(SmokeEffectPacket.class)
            .encoder(SmokeEffectPacket::encode)
            .decoder(SmokeEffectPacket::decode)
            .consumerNetworkThread(SmokeEffectPacket::handle)
            .add();
    }

    public void register(final RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
            BULLET_ENTITY_TYPE = EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .setTrackingRange(64).setUpdateInterval(5)
                .setShouldReceiveVelocityUpdates(false)
                .build(MODID + ":bullet");
            helper.register(new ResourceLocation(MODID, "bullet"), BULLET_ENTITY_TYPE);
        });
    }

    public void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(Items.MUSKET);
            event.accept(Items.MUSKET_WITH_BAYONET);
            event.accept(Items.PISTOL);
            event.accept(Items.CARTRIDGE);
        }
    }

    public void reload(final AddReloadListenerEvent event) {
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

    public static void sendSmokeEffect(LivingEntity shooter, Vec3 origin, Vec3 direction) {
        PacketDistributor.TargetPoint point = new PacketDistributor.TargetPoint(
            origin.x, origin.y, origin.z,
            64.0, shooter.level().dimension());
        NETWORK_CHANNEL.send(new SmokeEffectPacket(origin, direction),
            PacketDistributor.NEAR.with(point));
    }

    // TODO: use ewewukek.musketmod.SmokeEffectPacket when forge adds api for CustomPacketPayload
    public static class SmokeEffectPacket {
        public final Vec3 origin;
        public final Vec3 direction;

        public SmokeEffectPacket(Vec3 origin, Vec3 direction) {
            this.origin = origin;
            this.direction = direction;
        }

        public static SmokeEffectPacket decode(FriendlyByteBuf buf) {
            return new SmokeEffectPacket(
                new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat()),
                new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat()));
        }

        public static void encode(SmokeEffectPacket msg, FriendlyByteBuf buf) {
            buf.writeFloat((float)msg.origin.x);
            buf.writeFloat((float)msg.origin.y);
            buf.writeFloat((float)msg.origin.z);
            buf.writeFloat((float)msg.direction.x);
            buf.writeFloat((float)msg.direction.y);
            buf.writeFloat((float)msg.direction.z);
        }

        public static void handle(SmokeEffectPacket msg, CustomPayloadEvent.Context ctx) {
            ctx.enqueueWork(() -> {
                ClientSetup.handleSmokeEffectPacket(msg);
            });
            ctx.setPacketHandled(true);
        }
    }
}
