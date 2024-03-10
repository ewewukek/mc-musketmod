package ewewukek.musketmod;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
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

    public static EntityType<BulletEntity> BULLET_ENTITY_TYPE;

    private static final int PROTOCOL_VERSION = 2;
    public static final SimpleChannel NETWORK_CHANNEL = ChannelBuilder.named(
        new ResourceLocation(MODID, "main"))
        .networkProtocolVersion(PROTOCOL_VERSION)
        .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
        .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
        .simpleChannel();

    public MusketMod() {
        Config.reload();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init));
        NETWORK_CHANNEL.messageBuilder(SmokeEffectPacket.class)
            .encoder(SmokeEffectPacket::encode)
            .decoder(SmokeEffectPacket::decode)
            .consumerNetworkThread(SmokeEffectPacket::handle)
            .add();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onRegisterEvent(final RegisterEvent event) {
            event.register(ForgeRegistries.Keys.ITEMS, helper -> {
                helper.register("musket", Items.MUSKET);
                helper.register("musket_with_bayonet", Items.MUSKET_WITH_BAYONET);
                helper.register("pistol", Items.PISTOL);
                helper.register("cartridge", Items.CARTRIDGE);
            });

            event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
                BULLET_ENTITY_TYPE = EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .setTrackingRange(64).setUpdateInterval(5)
                    .setShouldReceiveVelocityUpdates(false)
                    .build(MODID + ":bullet");
                helper.register("bullet", BULLET_ENTITY_TYPE);
            });

            event.register(ForgeRegistries.Keys.SOUND_EVENTS, helper -> {
                helper.register(Sounds.MUSKET_LOAD_0.getLocation(), Sounds.MUSKET_LOAD_0);
                helper.register(Sounds.MUSKET_LOAD_1.getLocation(), Sounds.MUSKET_LOAD_1);
                helper.register(Sounds.MUSKET_LOAD_2.getLocation(), Sounds.MUSKET_LOAD_2);
                helper.register(Sounds.MUSKET_READY.getLocation(), Sounds.MUSKET_READY);
                helper.register(Sounds.MUSKET_FIRE.getLocation(), Sounds.MUSKET_FIRE);
                helper.register(Sounds.PISTOL_FIRE.getLocation(), Sounds.PISTOL_FIRE);
            });
        }

        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                event.accept(Items.MUSKET);
                event.accept(Items.MUSKET_WITH_BAYONET);
                event.accept(Items.PISTOL);
                event.accept(Items.CARTRIDGE);
            }
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

    public static void sendSmokeEffect(LivingEntity shooter, Vec3 origin, Vec3 direction) {
        NETWORK_CHANNEL.send(new SmokeEffectPacket(origin, direction),
            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(shooter));
    }

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
                ClientLevel level = Minecraft.getInstance().level;
                ClientSetup.handleSmokeEffectPacket(level, msg);
            });
            ctx.setPacketHandled(true);
        }
    }
}
