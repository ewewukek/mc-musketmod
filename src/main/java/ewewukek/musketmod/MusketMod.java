package ewewukek.musketmod;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

@Mod(MusketMod.MODID)
public class MusketMod {
    public static final String MODID = "musketmod";

    @ObjectHolder(MusketMod.MODID + ":musket")
    public static Item MUSKET;

    public MusketMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(BulletEntity.TYPE, (manager) -> new BulletRenderer(manager));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(
                new Item(new Item.Properties().group(ItemGroup.MISC)).setRegistryName(MODID, "barrel"),
                new Item(new Item.Properties().group(ItemGroup.MISC)).setRegistryName(MODID, "stock"),
                new Item(new Item.Properties().group(ItemGroup.COMBAT)).setRegistryName(MODID, "cartridge"),
                new MusketItem(new Item.Properties().group(ItemGroup.COMBAT)).setRegistryName(MODID, "musket")
            );
        }

        @SubscribeEvent
        public static void onEntityRegistry(final RegistryEvent.Register<EntityType<?>> event) {
            event.getRegistry().register(
                EntityType.Builder.<BulletEntity>create(EntityClassification.MISC)
                    .setCustomClientFactory(BulletEntity::new).size(0.5f, 0.5f)
                    .setTrackingRange(64).setUpdateInterval(5).setShouldReceiveVelocityUpdates(false)
                    .build(MODID + ":bullet").setRegistryName(MODID, "bullet")
            );
        }

        @SubscribeEvent
        public static void onSoundRegistry(final RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().registerAll(
                new SoundEvent(new ResourceLocation(MODID, "musket_load0")).setRegistryName(MODID, "musket_load0"),
                new SoundEvent(new ResourceLocation(MODID, "musket_load1")).setRegistryName(MODID, "musket_load1"),
                new SoundEvent(new ResourceLocation(MODID, "musket_load2")).setRegistryName(MODID, "musket_load2"),
                new SoundEvent(new ResourceLocation(MODID, "musket_ready")).setRegistryName(MODID, "musket_ready"),
                new SoundEvent(new ResourceLocation(MODID, "musket_fire")).setRegistryName(MODID, "musket_fire")
            );
        }

    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onRenderHandEvent(final RenderHandEvent event) {
            if (event.getHand() != Hand.MAIN_HAND) return;
            ItemStack stack = event.getItemStack();
            if (!stack.isEmpty() && stack.getItem() == MUSKET) {
                RenderHelper.renderSpecificFirstPersonHand(
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
}
