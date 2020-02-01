package ewewukek.musketmod;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

public class RenderHelper {
    private static int previousSlot = -1;
    public static boolean equipCycleCompleted;

    public static void renderSpecificFirstPersonHand(Hand hand, float partialTicks, float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack, MatrixStack matrixStack, IRenderTypeBuffer render, int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        HandSide handside = hand == Hand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        boolean isRightHand = handside == HandSide.RIGHT;
        float sign = isRightHand ? 1 : -1;

        int slot = player.inventory.currentItem;
        boolean slotChanged = slot != previousSlot;
        ItemStack clientStack = hand == Hand.MAIN_HAND ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
        if (slotChanged || clientStack.isEmpty() || clientStack.getItem() != MusketMod.MUSKET) equipCycleCompleted = false;

        matrixStack.push();

        if (swingProgress > 0) {
            float swingSharp = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
            float swingNormal = MathHelper.sin(swingProgress * (float)Math.PI);
            matrixStack.translate(sign * (0.2f - 0.05f * swingNormal), -0.2f - 0.05f * swingNormal, -0.3f - 0.4f * swingSharp);
            matrixStack.rotate(Vector3f.XP.rotationDegrees(180 + sign * (20 - 20 * swingSharp)));

        } else {
            float usingDuration = stack.getUseDuration() - (player.getItemInUseCount() - partialTicks + 1);
            boolean isLoading = player.isHandActive() && player.getActiveHand() == hand && !MusketItem.isLoaded(stack)
                                && usingDuration > 0 && usingDuration < MusketItem.RELOAD_DURATION;
            if (isLoading) {
                matrixStack.translate(sign * 0.15f, -0.55f, -0.3f);
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
                    matrixStack.translate(0, 0, 0.02f * t);
                }

            } else {
                if (equipCycleCompleted) {
                    equipProgress = 0;
                } else {
                    // postpone updating previousSlot because slot changing animation
                    // sometimes begins with equipProgress == 0
                    if (slotChanged) {
                        if (equipProgress > 0.1) previousSlot = slot;
                    } else {
                        if (equipProgress == 0f) equipCycleCompleted = true;
                    }
                }
                matrixStack.translate(sign * 0.15f, -0.27f + equipProgress * -0.6f, -0.37f);
            }
        }

        // compensate rotated model
        matrixStack.translate(0, 0.085f, 0);
        matrixStack.rotate(Vector3f.XP.rotationDegrees(-70));

        mc.getFirstPersonRenderer().renderItemSide(player, stack, isRightHand ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !isRightHand, matrixStack, render, packedLight);
        matrixStack.pop();
    }
}
