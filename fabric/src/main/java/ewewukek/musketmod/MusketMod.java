package ewewukek.musketmod;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.phys.Vec3;

public class MusketMod implements ModInitializer {
    public static final String MODID = "musketmod";
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("musketmod.txt");

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }

    @Override
    public void onInitialize() {
        Config.load();

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

        BulletEntity.ENTITY_TYPE = FabricEntityTypeBuilder.<BulletEntity>create(MobCategory.MISC, BulletEntity::new)
            .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
            .trackRangeBlocks(64).trackedUpdateRate(20)
            .forceTrackedVelocityUpdates(false)
            .build();
        Registry.register(BuiltInRegistries.ENTITY_TYPE, resource("bullet"), BulletEntity.ENTITY_TYPE);

        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            DeferredDamage.apply();
        });

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

    public static final ResourceLocation SMOKE_EFFECT_PACKET_ID = new ResourceLocation(MODID, "smoke_effect");

    public static void sendSmokeEffect(ServerLevel level, Vec3 origin, Vec3 direction) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeFloat((float)origin.x);
        buf.writeFloat((float)origin.y);
        buf.writeFloat((float)origin.z);
        buf.writeFloat((float)direction.x);
        buf.writeFloat((float)direction.y);
        buf.writeFloat((float)direction.z);
        BlockPos blockPos = BlockPos.containing(origin.x, origin.y, origin.z);
        for (ServerPlayer serverPlayer : PlayerLookup.tracking(level, blockPos)) {
            ServerPlayNetworking.send(serverPlayer, SMOKE_EFFECT_PACKET_ID, buf);
        }
    }
}
