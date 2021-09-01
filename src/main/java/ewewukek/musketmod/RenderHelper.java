package ewewukek.musketmod;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderHelper {
    public static boolean disableMainHandEquipAnimation;
    public static boolean disableOffhandEquipAnimation;

    public static void renderSpecificFirstPersonHand(FirstPersonRenderer renderer, AbstractClientPlayerEntity player, Hand hand, float partialTicks, float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack, MatrixStack matrixStack, IRenderTypeBuffer render, int packedLight) {
        HandSide handside = hand == Hand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        boolean isRightHand = handside == HandSide.RIGHT;
        float sign = isRightHand ? 1 : -1;

        GunItem gunItem = (GunItem)stack.getItem();
        if (!gunItem.canUseFrom(player, hand)) {
            matrixStack.push();
            matrixStack.translate(sign * 0.5, -0.5 - 0.6 * equipProgress, -0.7);
            matrixStack.rotate(Vector3f.XP.rotationDegrees(70));
            renderer.renderItemSide(player, stack, isRightHand ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !isRightHand, matrixStack, render, packedLight);
            matrixStack.pop();
            return;
        }

        if (stack == GunItem.getActiveStack(hand)) {
            setEquipAnimationDisabled(hand, true);
        }

        matrixStack.push();
        matrixStack.translate(sign * 0.15, -0.25, -0.35);

        if (swingProgress > 0) {
            float swingSharp = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
            float swingNormal = MathHelper.sin(swingProgress * (float)Math.PI);

            if (gunItem == MusketMod.MUSKET_WITH_BAYONET) {
                matrixStack.translate(sign * -0.05 * swingNormal, 0, 0.05 - 0.3 * swingSharp);
                matrixStack.rotate(Vector3f.YP.rotationDegrees(5 * swingSharp));
            } else {
                matrixStack.translate(sign * 0.05 * (1 - swingNormal), 0.05 * (1 - swingNormal), 0.05 - 0.4 * swingSharp);
                matrixStack.rotate(Vector3f.XP.rotationDegrees(180 + sign * 20 * (1 - swingSharp)));
            }

        } else if (player.isHandActive() && player.getActiveHand() == hand) {
            float usingDuration = stack.getUseDuration() - (player.getItemInUseCount() - partialTicks + 1);
            if (usingDuration > 0 && usingDuration < GunItem.RELOAD_DURATION) {
                matrixStack.translate(0, -0.3, 0.05);
                matrixStack.rotate(Vector3f.XP.rotationDegrees(60));
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(10));

                if (usingDuration >= 8 && usingDuration <= 14 || usingDuration >= 18 && usingDuration <= 24) {
                    if (usingDuration >= 18) usingDuration -= 10;
                    float t;
                    if (usingDuration < 10) {
                        t = (usingDuration - 8) / 2;
                        t = MathHelper.sin((float)Math.PI / 2 * MathHelper.sqrt(t));
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
            if (isEquipAnimationDisabled(hand)) {
                if (equipProgress == 0) {
                    setEquipAnimationDisabled(hand, false);
                }
            } else {
                matrixStack.translate(0, -0.6 * equipProgress, 0);
            }
        }

        renderer.renderItemSide(player, stack, isRightHand ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !isRightHand, matrixStack, render, packedLight);
        matrixStack.pop();
    }

    public static boolean isEquipAnimationDisabled(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return disableMainHandEquipAnimation;
        } else {
            return disableOffhandEquipAnimation;
        }
    }

    public static void setEquipAnimationDisabled(Hand hand, boolean disabled) {
        if (hand == Hand.MAIN_HAND) {
            disableMainHandEquipAnimation = disabled;
        } else {
            disableOffhandEquipAnimation = disabled;
        }
    }
}
