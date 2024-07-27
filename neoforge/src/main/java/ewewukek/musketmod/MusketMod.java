package ewewukek.musketmod;

import java.nio.file.Path;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("musketmod.txt");

    public static EntityType<BulletEntity> BULLET_ENTITY_TYPE;

    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
        DeferredRegister.createDataComponents(MODID);
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);

    public MusketMod(IEventBus bus, ModContainer modContainer) {
        Config.reload();

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

        bus.addListener(this::register);
        bus.addListener(this::creativeTabs);
        bus.addListener(this::registerPacket);
    }

    public void register(final RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE, helper -> {
            BULLET_ENTITY_TYPE = EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .setTrackingRange(64).setUpdateInterval(5)
                .setShouldReceiveVelocityUpdates(false)
                .build(MODID + ":bullet");
            helper.register(new ResourceLocation(MODID, "bullet"), BULLET_ENTITY_TYPE);
        });
    }

    public void creativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(Items.MUSKET);
            event.accept(Items.MUSKET_WITH_BAYONET);
            event.accept(Items.PISTOL);
            event.accept(Items.CARTRIDGE);
        }
    }

    public void registerPacket(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MusketMod.MODID).versioned("2").optional();
        registrar.playToClient(
            SmokeEffectPacket.TYPE,
            SmokeEffectPacket.CODEC,
            (packet, context) -> {
                context.enqueueWork(() -> {
                    ClientSetup.handleSmokeEffectPacket(packet);
                });
            }
        );
    }

    public static void sendSmokeEffect(LivingEntity shooter, Vec3 origin, Vec3 direction) {
        SmokeEffectPacket packet = SmokeEffectPacket.fromVec3(origin, direction);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(shooter, packet);
    }
}
