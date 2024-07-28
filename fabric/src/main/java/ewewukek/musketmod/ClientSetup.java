package ewewukek.musketmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

public class ClientSetup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientUtilities.registerItemProperties();

        EntityRendererRegistry.register(BulletEntity.ENTITY_TYPE, (context) -> new BulletRenderer(context));

        ClientPlayNetworking.registerGlobalReceiver(MusketMod.SMOKE_EFFECT_PACKET_ID, (client, handler, buf, responseSender) -> {
            ClientLevel world = handler.getLevel();
            Vec3 origin = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
            Vec3 direction = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
            client.execute(() -> GunItem.fireParticles(world, origin, direction));
        });
    }
}
