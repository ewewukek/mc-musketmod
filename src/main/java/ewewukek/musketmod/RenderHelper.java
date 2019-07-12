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
    // copies renderItemInFirstPerson
    public static void renderSpecificFirstPersonHand(Hand hand, float partialTicks, float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        HandSide handside = hand == Hand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        float xSign = handside == HandSide.RIGHT ? 1 : -1;

        GlStateManager.pushMatrix();

        float swingX = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
        float swingY = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F));
        float swingZ = -0.2F * MathHelper.sin(swingProgress * (float)Math.PI);
        GlStateManager.translatef(xSign * swingX, swingY, swingZ);

        // transformSideFirstPerson
        GlStateManager.translatef(xSign * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);

        // transformFirstPerson
        float swingFactor = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        GlStateManager.rotatef(xSign * (45.0F + swingFactor * -20.0F), 0.0F, 1.0F, 0.0F);
        float swingRotation = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
        GlStateManager.rotatef(xSign * swingRotation * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotatef(swingRotation * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(xSign * -45.0F, 0.0F, 1.0F, 0.0F);

        mc.getFirstPersonRenderer().renderItemSide(player, stack, handside == HandSide.RIGHT ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, handside == HandSide.LEFT);

        GlStateManager.popMatrix();
     }


}
