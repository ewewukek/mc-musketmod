package ewewukek.musketmod;

import java.nio.file.Path;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
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
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("musketmod.txt");

    public MusketMod(IEventBus bus, ModContainer modContainer) {
        Config.reload();

        bus.addListener(this::register);
        bus.addListener(this::creativeTabs);
        bus.addListener(this::registerPacket);
    }

    public void register(final RegisterEvent event) {
        Items.registerDataComponentTypes((path, component) -> {
            event.register(Registries.DATA_COMPONENT_TYPE, new ResourceLocation(MODID, path), () -> component);
        });
        Items.register((path, item) -> {
            event.register(Registries.ITEM, new ResourceLocation(MODID, path), () -> item);
        });
        Sounds.register((sound) -> {
            event.register(Registries.SOUND_EVENT, sound.getLocation(), () -> sound);
        });
        event.register(Registries.ENTITY_TYPE, helper -> {
            BulletEntity.register((string, entityType) -> {
                helper.register(new ResourceLocation(MODID, string), entityType);
            });
        });
    }

    public void creativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            Items.addToCombatTab((item) -> {
                event.accept(item);
            });
        }
    }

    public void registerPacket(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MusketMod.MODID).versioned("2").optional();
        registrar.playToClient(
            SmokeEffectPacket.TYPE,
            SmokeEffectPacket.CODEC,
            (packet, context) -> {
                context.enqueueWork(() -> {
                    ClientUtilities.handleSmokeEffectPacket(packet);
                });
            }
        );
    }

    public static void sendSmokeEffect(LivingEntity shooter, Vec3 origin, Vec3 direction) {
        SmokeEffectPacket packet = SmokeEffectPacket.fromVec3(origin, direction);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(shooter, packet);
    }
}
