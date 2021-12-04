package ewewukek.musketmod;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        ItemPropertyFunction loaded = (stack, world, player, arg) -> {
            return GunItem.isLoaded(stack) ? 1 : 0;
        };
        ItemProperties.register(Items.MUSKET, new ResourceLocation("loaded"), loaded);
        ItemProperties.register(Items.MUSKET_WITH_BAYONET, new ResourceLocation("loaded"), loaded);
        ItemProperties.register(Items.PISTOL, new ResourceLocation("loaded"), loaded);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderHandEvent(final RenderHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() instanceof GunItem) {
            Minecraft mc = Minecraft.getInstance();
            ClientUtilities.renderGunInHand(
                    mc.getItemInHandRenderer(), mc.player,
                    event.getHand(), event.getPartialTicks(), event.getInterpolatedPitch(),
                    event.getSwingProgress(), event.getEquipProgress(), stack,
                    event.getMatrixStack(), event.getBuffers(), event.getLight());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLivingEventPre(final RenderLivingEvent.Pre<Player, PlayerModel<Player>> event) {
        if (!(event.getEntity() instanceof Player)
         || !(event.getRenderer().getModel() instanceof PlayerModel)) return;
        Player player = (Player)event.getEntity();
        HumanoidModel.ArmPose mainHandPose = ClientUtilities.getArmPose(player, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offhandPose = ClientUtilities.getArmPose(player, InteractionHand.OFF_HAND);
        PlayerModel<Player> model = event.getRenderer().getModel();
        if (player.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = mainHandPose != null ? mainHandPose : model.rightArmPose;
            model.leftArmPose = offhandPose != null ? offhandPose : model.leftArmPose;
        } else {
            model.rightArmPose = offhandPose != null ? offhandPose : model.rightArmPose;
            model.leftArmPose = mainHandPose != null ? mainHandPose : model.leftArmPose;
        }
    }

    public static void handleSmokeEffectPacket(MusketMod.SmokeEffectPacket packet, Supplier<NetworkEvent.Context> ctx) {
        PacketListener listener = ctx.get().getNetworkManager().getPacketListener();
        if (listener instanceof ClientPacketListener) {
            GunItem.fireParticles(((ClientPacketListener)listener).getLevel(), packet.origin, packet.direction);
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(MusketMod.BULLET_ENTITY_TYPE, BulletRenderer::new);
        }
    }
}
