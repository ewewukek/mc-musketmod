package ewewukek.musketmod;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ClientUtilities {
    public static void registerItemProperties() {
        ClampedItemPropertyFunction predicate = (stack, level, player, seed) -> {
            return GunItem.isLoaded(stack) ? 1 : 0;
        };
        ResourceLocation location = MusketMod.resource("loaded");
        ItemProperties.register(Items.MUSKET, location, predicate);
        ItemProperties.register(Items.MUSKET_WITH_BAYONET, location, predicate);
        ItemProperties.register(Items.MUSKET_WITH_SCOPE, location, predicate);
        ItemProperties.register(Items.BLUNDERBUSS, location, predicate);
        ItemProperties.register(Items.PISTOL, location, predicate);
    }

    public static void handleSmokeEffectPacket(SmokeEffectPacket packet) {
        Minecraft instance = Minecraft.getInstance();
        ClientLevel level = instance.level;
        Vec3 origin = new Vec3(packet.origin());
        Vec3 direction = new Vec3(packet.direction());
        GunItem.fireParticles(level, origin, direction);
    }

    public static Optional<HumanoidModel.ArmPose> getArmPose(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.swinging && !stack.isEmpty() && stack.getItem() instanceof GunItem gun) {
            if (gun.canUseFrom(player, hand) && GunItem.isLoaded(stack)) {
                return Optional.of(HumanoidModel.ArmPose.CROSSBOW_HOLD);
            }
        }
        return Optional.empty();
    }

    public static boolean disableMainHandEquipAnimation;
    public static boolean disableOffhandEquipAnimation;

    public static void renderGunInHand(ItemInHandRenderer renderer, AbstractClientPlayer player, InteractionHand hand, float dt, float pitch, float swingProgress, float equipProgress, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (player.isScoping()) {
            return;
        }

        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean isRightHand = arm == HumanoidArm.RIGHT;
        float sign = isRightHand ? 1 : -1;

        GunItem gun = (GunItem)stack.getItem();
        if (!gun.canUseFrom(player, hand)) {
            poseStack.pushPose();
            poseStack.translate(sign * 0.5, -0.5 - 0.6 * equipProgress, -0.7);
            poseStack.mulPose(Axis.XP.rotationDegrees(70));
            renderer.renderItem(player, stack, isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !isRightHand, poseStack, bufferSource, light);
            poseStack.popPose();
            return;
        }

        if (stack == GunItem.getActiveStack(hand)) {
            setEquipAnimationDisabled(hand, true);
        }

        poseStack.pushPose();
        poseStack.translate(sign * 0.15, -0.25, -0.35);

        if (swingProgress > 0) {
            float swingSharp = Mth.sin(Mth.sqrt(swingProgress) * (float)Math.PI);
            float swingNormal = Mth.sin(swingProgress * (float)Math.PI);

            if (gun == Items.MUSKET_WITH_BAYONET) {
                poseStack.translate(sign * -0.05 * swingNormal, 0, 0.05 - 0.3 * swingSharp);
                poseStack.mulPose(Axis.YP.rotationDegrees(5 * swingSharp));
            } else {
                poseStack.translate(sign * 0.05 * (1 - swingNormal), 0.05 * (1 - swingNormal), 0.05 - 0.4 * swingSharp);
                poseStack.mulPose(Axis.XP.rotationDegrees(180 + sign * 20 * (1 - swingSharp)));
            }

        } else if (player.isUsingItem() && player.getUsedItemHand() == hand) {
            int stageDuration = GunItem.TICKS_PER_LOADING_STAGE;
            int loadingStage = GunItem.getLoadingStage(stack) + player.getTicksUsingItem() / stageDuration;
            float useTicks = player.getTicksUsingItem() + dt - 1;
            int reloadDuration = GunItem.reloadDuration(stack);
            if (reloadDuration > 0 && useTicks < reloadDuration + 5) {
                poseStack.translate(0, -0.3, 0.05);
                poseStack.mulPose(Axis.XP.rotationDegrees(60));
                poseStack.mulPose(Axis.ZP.rotationDegrees(10));

                float t = 0;
                if (useTicks >= stageDuration && loadingStage <= GunItem.LOADING_STAGES) {
                    useTicks = useTicks % stageDuration;
                    if (useTicks < 4) {
                        t = (4 - useTicks) / 4;
                    }
                }
                if (useTicks >= stageDuration - 2 && loadingStage < GunItem.LOADING_STAGES) {
                    t = (useTicks - stageDuration + 2) / 2;
                    t = Mth.sin((float)Math.PI / 2 * Mth.sqrt(t));
                }
                poseStack.translate(0, 0, 0.025 * t);

                if (gun == Items.BLUNDERBUSS) {
                    poseStack.translate(0, 0, -0.06);
                } else if (gun == Items.PISTOL) {
                    poseStack.translate(0, 0, -0.12);
                }
            }
        } else {
            if (isEquipAnimationDisabled(hand)) {
                if (equipProgress == 0) {
                    setEquipAnimationDisabled(hand, false);
                }
            } else {
                poseStack.translate(0, -0.6 * equipProgress, 0);
            }
        }

        renderer.renderItem(player, stack, isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !isRightHand, poseStack, bufferSource, light);
        poseStack.popPose();
    }

    public static boolean isEquipAnimationDisabled(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return disableMainHandEquipAnimation;
        } else {
            return disableOffhandEquipAnimation;
        }
    }

    public static void setEquipAnimationDisabled(InteractionHand hand, boolean disabled) {
        if (hand == InteractionHand.MAIN_HAND) {
            disableMainHandEquipAnimation = disabled;
        } else {
            disableOffhandEquipAnimation = disabled;
        }
    }
}
