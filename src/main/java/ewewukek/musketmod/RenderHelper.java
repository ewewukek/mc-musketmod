package ewewukek.musketmod;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

public class RenderHelper {
    public static void renderSpecificFirstPersonHand(Hand hand, float partialTicks, float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        HandSide handside = hand == Hand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        boolean isRightHand = handside == HandSide.RIGHT;
        float sign = isRightHand ? 1 : -1;

        GlStateManager.pushMatrix();

        if (swingProgress > 0) {
            float swingSharp = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
            float swingNormal = MathHelper.sin(swingProgress * (float)Math.PI);
            GlStateManager.translatef(sign * (0.2f - 0.05f * swingNormal), -0.2f - 0.05f * swingNormal, -0.3f - 0.4f * swingSharp);
            GlStateManager.rotatef(180 + sign * (20 - 20 * swingSharp), 1, 0, 0);

        } else {
            float usingDuration = stack.getUseDuration() - (player.getItemInUseCount() - partialTicks + 1);
            boolean isLoading = player.isHandActive() && player.getActiveHand() == hand && !MusketItem.isLoaded(stack)
                                && usingDuration > 0 && usingDuration < MusketItem.RELOAD_DURATION;
            if (isLoading) {
                GlStateManager.translatef(sign * 0.15f, -0.55f, -0.3f);
                GlStateManager.rotatef(60, 1, 0, 0);
                GlStateManager.rotatef(10, 0, 0, 1);

                if (usingDuration >= 8 && usingDuration <= 14 || usingDuration >= 18 && usingDuration <= 24) {
                    if (usingDuration >= 18) usingDuration -= 10;
                    float t;
                    if (usingDuration < 10) {
                        t = (usingDuration - 8) / 2;
                        t = MathHelper.sin((float)Math.PI / 2 * MathHelper.sqrt(t));
                    } else {
                        t = (14 - usingDuration) / 4;
                    }
                    GlStateManager.translatef(0, 0, 0.02f * t);
                }

            } else {
                equipProgress = 0;
                GlStateManager.translatef(sign * 0.15f, -0.27f + equipProgress * -0.6f, -0.37f);
            }
        }

        mc.getFirstPersonRenderer().renderItemSide(player, stack, isRightHand ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !isRightHand);

        GlStateManager.popMatrix();
     }


}
