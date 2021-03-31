package ewewukek.musketmod;

import static ewewukek.musketmod.MusketMod.MUSKET;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientSetup {

    public static void init(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(MusketMod.BULLET_ENTITY_TYPE, BulletRenderer::new);
        ItemModelsProperties.registerProperty(MUSKET, new ResourceLocation("loaded"), (stack, world, player) -> {
            return MusketItem.isLoaded(stack) ? 1 : 0;
        });
    }

    @SubscribeEvent
    public static void onRenderHandEvent(final RenderHandEvent event) {
        if (event.getHand() != Hand.MAIN_HAND) return;
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() == MUSKET) {
            Minecraft mc = Minecraft.getInstance();
            RenderHelper.renderSpecificFirstPersonHand(
                    mc.getFirstPersonRenderer(), mc.player,
                    event.getHand(), event.getPartialTicks(), event.getInterpolatedPitch(),
                    event.getSwingProgress(), event.getEquipProgress(), stack,
                    event.getMatrixStack(), event.getBuffers(), event.getLight());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLivingEventPre(final RenderLivingEvent.Pre<PlayerEntity, PlayerModel<PlayerEntity>> event) {
        if (!(event.getEntity() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity)event.getEntity();
        if (player.isSwingInProgress) return;
        ItemStack stack = player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem() == MUSKET && MusketItem.isLoaded(stack)) {
            PlayerModel<PlayerEntity> model = event.getRenderer().getEntityModel();
            if (player.getPrimaryHand() == HandSide.RIGHT) {
                model.rightArmPose = BipedModel.ArmPose.CROSSBOW_HOLD;
            } else {
                model.leftArmPose = BipedModel.ArmPose.CROSSBOW_HOLD;
            }
        }
    }
}
