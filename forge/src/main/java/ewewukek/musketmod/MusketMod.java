package ewewukek.musketmod;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("musketmod.txt");

    public static final EntityType<BulletEntity> BULLET_ENTITY_TYPE = EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .setTrackingRange(64).setUpdateInterval(5)
            .setShouldReceiveVelocityUpdates(false)
            .build(MODID + ":bullet");

    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MODID, "main"), () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public MusketMod() {
        Config.reload();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init));
        NETWORK_CHANNEL.registerMessage(1, SmokeEffectPacket.class,
            SmokeEffectPacket::encode, SmokeEffectPacket::new, SmokeEffectPacket::handle);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(
                Items.MUSKET.setRegistryName(MODID, "musket"),
                Items.MUSKET_WITH_BAYONET.setRegistryName(MODID, "musket_with_bayonet"),
                Items.PISTOL.setRegistryName(MODID, "pistol"),
                Items.CARTRIDGE.setRegistryName(MODID, "cartridge")
            );
        }

        @SubscribeEvent
        public static void onEntityRegistry(final RegistryEvent.Register<EntityType<?>> event) {
            event.getRegistry().register(
                BULLET_ENTITY_TYPE.setRegistryName(MODID, "bullet")
            );
        }

        @SubscribeEvent
        public static void onSoundRegistry(final RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().registerAll(
                Sounds.MUSKET_LOAD_0.setRegistryName(Sounds.MUSKET_LOAD_0.getLocation()),
                Sounds.MUSKET_LOAD_1.setRegistryName(Sounds.MUSKET_LOAD_1.getLocation()),
                Sounds.MUSKET_LOAD_2.setRegistryName(Sounds.MUSKET_LOAD_2.getLocation()),
                Sounds.MUSKET_READY.setRegistryName(Sounds.MUSKET_READY.getLocation()),
                Sounds.MUSKET_FIRE.setRegistryName(Sounds.MUSKET_FIRE.getLocation()),
                Sounds.PISTOL_FIRE.setRegistryName(Sounds.PISTOL_FIRE.getLocation())
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

    public static void sendSmokeEffect(LivingEntity shooter, Vec3 origin, Vec3 direction) {
        NETWORK_CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> shooter),
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
