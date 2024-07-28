package ewewukek.musketmod;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.fabricmc.api.ModInitializer;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.phys.Vec3;

public class MusketMod implements ModInitializer {
    public static final String MODID = "musketmod";
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("musketmod.txt");

    @Override
    public void onInitialize() {
        Config.reload();

        Items.registerDataComponentTypes((path, component) -> {
            Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, new ResourceLocation(MODID, path), component);
        });
        Items.register((path, item) -> {
            Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MODID, path), item);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            Items.addToCombatTab((item) -> {
                entries.accept(item);
            });
        });
        Sounds.register((sound) -> {
            Registry.register(BuiltInRegistries.SOUND_EVENT, sound.getLocation(), sound);
        });
        BulletEntity.register((string, entityType) -> {
            Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation(MODID, string), entityType);
        });

        PayloadTypeRegistry.playS2C().register(SmokeEffectPacket.TYPE, SmokeEffectPacket.CODEC);

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(MODID, "reload");
            }

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

    public static void disableVelocityUpdate(EntityType.Builder<?> builder) {
        builder.alwaysUpdateVelocity(false);
    }

    public static void sendSmokeEffect(LivingEntity shooter, Vec3 origin, Vec3 direction) {
        SmokeEffectPacket packet = SmokeEffectPacket.fromVec3(origin, direction);
        BlockPos blockPos = BlockPos.containing(origin.x, origin.y, origin.z);
        for (ServerPlayer serverPlayer : PlayerLookup.tracking((ServerLevel)shooter.level(), blockPos)) {
            ServerPlayNetworking.send(serverPlayer, packet);
        }
    }
}
