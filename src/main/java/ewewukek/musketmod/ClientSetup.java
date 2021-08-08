package ewewukek.musketmod;

import static ewewukek.musketmod.MusketMod.MUSKET;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        ItemProperties.register(MUSKET, new ResourceLocation("loaded"), (stack, world, player, arg) -> {
            return MusketItem.isLoaded(stack) ? 1 : 0;
        });
    }

    @SubscribeEvent
    public static void onRenderHandEvent(final RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() == MUSKET) {
            Minecraft mc = Minecraft.getInstance();
            RenderHelper.renderSpecificFirstPersonHand(
                    mc.getItemInHandRenderer(), mc.player,
                    event.getHand(), event.getPartialTicks(), event.getInterpolatedPitch(),
                    event.getSwingProgress(), event.getEquipProgress(), stack,
                    event.getMatrixStack(), event.getBuffers(), event.getLight());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLivingEventPre(final RenderLivingEvent.Pre<Player, PlayerModel<Player>> event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player)event.getEntity();
        if (player.swinging) return;
        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() == MUSKET && MusketItem.isLoaded(stack)) {
            PlayerModel<Player> model = event.getRenderer().getModel();
            if (player.getMainArm() == HumanoidArm.RIGHT) {
                model.rightArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
            } else {
                model.leftArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
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
