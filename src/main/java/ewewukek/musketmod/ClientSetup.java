package ewewukek.musketmod;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientSetup {

    public static void init(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(MusketMod.BULLET_ENTITY_TYPE, BulletRenderer::new);
        IItemPropertyGetter loaded = (stack, world, player) -> {
            return GunItem.isLoaded(stack) ? 1 : 0;
        };
        ItemModelsProperties.registerProperty(MusketMod.MUSKET, new ResourceLocation("loaded"), loaded);
        ItemModelsProperties.registerProperty(MusketMod.MUSKET_WITH_BAYONET, new ResourceLocation("loaded"), loaded);
        ItemModelsProperties.registerProperty(MusketMod.PISTOL, new ResourceLocation("loaded"), loaded);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderHandEvent(final RenderHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() instanceof GunItem) {
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
        if (!(event.getEntity() instanceof PlayerEntity)
         || !(event.getRenderer().getEntityModel() instanceof PlayerModel)) return;

        PlayerEntity player = (PlayerEntity)event.getEntity();
        if (player.isSwingInProgress) return;
        Optional<BipedModel.ArmPose> rightArmPose;
        Optional<BipedModel.ArmPose> leftArmPose;
        if (player.getPrimaryHand() == HandSide.RIGHT) {
            rightArmPose = getArmPose(player, Hand.MAIN_HAND);
            leftArmPose = getArmPose(player, Hand.OFF_HAND);
        } else {
            rightArmPose = getArmPose(player, Hand.OFF_HAND);
            leftArmPose = getArmPose(player, Hand.MAIN_HAND);
        }
        PlayerModel<PlayerEntity> model = event.getRenderer().getEntityModel();
        if (rightArmPose.isPresent()) model.rightArmPose = rightArmPose.get();
        if (leftArmPose.isPresent()) model.leftArmPose = leftArmPose.get();
    }

    public static Optional<BipedModel.ArmPose> getArmPose(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && stack.getItem() instanceof GunItem) {
            GunItem gunItem = (GunItem)stack.getItem();
            if (gunItem.canUseFrom(player, hand) && GunItem.isLoaded(stack)) {
                return Optional.of(BipedModel.ArmPose.CROSSBOW_HOLD);
            }
        }
        return Optional.empty();
    }

    public static void handleSmokeEffectPacket(MusketMod.SmokeEffectPacket packet, Supplier<NetworkEvent.Context> ctx) {
        INetHandler listener = ctx.get().getNetworkManager().getNetHandler();
        if (listener instanceof ClientPlayNetHandler) {
            GunItem.fireParticles(((ClientPlayNetHandler)listener).getWorld(), packet.origin, packet.direction);
        }
    }
}
