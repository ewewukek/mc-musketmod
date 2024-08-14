package ewewukek.musketmod;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.phys.Vec3;

public class MusketMod implements ModInitializer {
    public static final String MODID = "musketmod";
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("musketmod.txt");

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    @Override
    public void onInitialize() {
        Config.load();

        Items.registerDataComponentTypes((path, component) -> {
            Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, resource(path), component);
        });
        Items.register((path, item) -> {
            Registry.register(BuiltInRegistries.ITEM, resource(path), item);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            Items.addToCreativeTab(CreativeModeTabs.COMBAT, (item) -> {
                entries.accept(item);
            });
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            Items.addToCreativeTab(CreativeModeTabs.INGREDIENTS, (item) -> {
                entries.accept(item);
            });
        });
        Sounds.register((sound) -> {
            Registry.register(BuiltInRegistries.SOUND_EVENT, sound.getLocation(), sound);
        });
        BulletEntity.register((string, entityType) -> {
            Registry.register(BuiltInRegistries.ENTITY_TYPE, resource(string), entityType);
        });

        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            DeferredDamage.apply();
        });

        PayloadTypeRegistry.playS2C().register(SmokeEffectPacket.TYPE, SmokeEffectPacket.CODEC);

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return resource("reload");
            }

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
        builder.alwaysUpdateVelocity(false);
    }

    public static void sendSmokeEffect(ServerLevel level, Vec3 origin, Vec3 direction) {
        SmokeEffectPacket packet = SmokeEffectPacket.fromVec3(origin, direction);
        BlockPos blockPos = BlockPos.containing(origin.x, origin.y, origin.z);
        for (ServerPlayer serverPlayer : PlayerLookup.tracking(level, blockPos)) {
            ServerPlayNetworking.send(serverPlayer, packet);
        }
    }
}
