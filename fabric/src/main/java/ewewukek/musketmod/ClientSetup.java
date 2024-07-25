package ewewukek.musketmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class ClientSetup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(MusketMod.BULLET_ENTITY_TYPE, (ctx) -> new BulletRenderer(ctx));

        ClampedItemPropertyFunction loaded = (stack, world, player, seed) -> {
            return GunItem.isLoaded(stack) ? 1 : 0;
        };
        ItemProperties.register(Items.MUSKET, new ResourceLocation("loaded"), loaded);
        ItemProperties.register(Items.MUSKET_WITH_BAYONET, new ResourceLocation("loaded"), loaded);
        ItemProperties.register(Items.PISTOL, new ResourceLocation("loaded"), loaded);

        ClientPlayNetworking.registerGlobalReceiver(SmokeEffectPacket.TYPE, (packet, context) -> {
            Vec3 origin = new Vec3(packet.origin());
            Vec3 direction = new Vec3(packet.direction());
            context.client().execute(() -> {
                GunItem.fireParticles(context.player().level(), origin, direction);
            });
        });
    }
}
