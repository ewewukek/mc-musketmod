package ewewukek.musketmod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class RenderHelper {
    public static boolean disableEquipAnimation;

    public static void renderSpecificFirstPersonHand(ItemInHandRenderer renderer, AbstractClientPlayer player, InteractionHand hand, float partialTicks, float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack, PoseStack matrixStack, MultiBufferSource render, int packedLight) {
        HumanoidArm handside = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean isRightHand = handside == HumanoidArm.RIGHT;
        float sign = isRightHand ? 1 : -1;

        GunItem gunItem = (GunItem)stack.getItem();
        if (!gunItem.canUseFrom(player, hand)) {
            matrixStack.pushPose();
            matrixStack.translate(sign * 0.5, -0.5 - 0.6 * equipProgress, -0.7);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(70));
            renderer.renderItem(player, stack, isRightHand ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !isRightHand, matrixStack, render, packedLight);
            matrixStack.popPose();
            return;
        }

        if (stack == GunItem.activeStack) {
            disableEquipAnimation = true;
        }

        matrixStack.pushPose();
        matrixStack.translate(sign * 0.15, -0.25, -0.35);

        if (swingProgress > 0) {
            float swingSharp = Mth.sin(Mth.sqrt(swingProgress) * (float)Math.PI);
            float swingNormal = Mth.sin(swingProgress * (float)Math.PI);

            if (gunItem == MusketMod.MUSKET_WITH_BAYONET) {
                matrixStack.translate(sign * -0.05 * swingNormal, 0, 0.05 - 0.3 * swingSharp);
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(5 * swingSharp));
            } else {
                matrixStack.translate(sign * 0.05 * (1 - swingNormal), 0.05 * (1 - swingNormal), 0.05 - 0.4 * swingSharp);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(180 + sign * 20 * (1 - swingSharp)));
            }

        } else if (player.isUsingItem() && player.getUsedItemHand() == hand) {
            float usingDuration = stack.getUseDuration() - (player.getUseItemRemainingTicks() - partialTicks + 1);
            if (usingDuration > 0 && usingDuration < GunItem.RELOAD_DURATION) {
                matrixStack.translate(0, -0.3, 0.05);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(60));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(10));

                if (usingDuration >= 8 && usingDuration <= 14 || usingDuration >= 18 && usingDuration <= 24) {
                    if (usingDuration >= 18) usingDuration -= 10;
                    float t;
                    if (usingDuration < 10) {
                        t = (usingDuration - 8) / 2;
                        t = Mth.sin((float)Math.PI / 2 * Mth.sqrt(t));
                    } else {
                        t = (14 - usingDuration) / 4;
                    }
                    matrixStack.translate(0, 0, 0.025 * t);
                }
                if (gunItem == MusketMod.PISTOL) {
                    matrixStack.translate(0, 0, -0.12);
                }
            }
        } else {
            if (disableEquipAnimation) {
                if (equipProgress == 0) {
                    disableEquipAnimation = false;
                }
            } else {
                matrixStack.translate(0, -0.6 * equipProgress, 0);
            }
        }

        renderer.renderItem(player, stack, isRightHand ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !isRightHand, matrixStack, render, packedLight);
        matrixStack.popPose();
    }
}
