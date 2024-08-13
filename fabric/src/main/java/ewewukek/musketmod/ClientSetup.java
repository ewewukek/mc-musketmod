package ewewukek.musketmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ClientSetup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientUtilities.registerItemProperties();

        EntityRendererRegistry.register(BulletEntity.ENTITY_TYPE, (context) -> new BulletRenderer(context));

        ClientPlayNetworking.registerGlobalReceiver(SmokeEffectPacket.TYPE, (packet, context) -> {
            context.client().execute(() -> {
                ClientUtilities.handleSmokeEffectPacket(packet);
            });
        });
    }
}
