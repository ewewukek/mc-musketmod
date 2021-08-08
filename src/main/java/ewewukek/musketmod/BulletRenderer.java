package ewewukek.musketmod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BulletRenderer extends EntityRenderer<BulletEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(MusketMod.MODID + ":textures/entity/bullet.png");

    public BulletRenderer(EntityRendererProvider.Context manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getTextureLocation(BulletEntity bullet) {
        return TEXTURE;
    }

    @Override
    public void render(BulletEntity bullet, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource render, int packedLight) {
        if (bullet.isFirstTick()) return;

        matrixStack.pushPose();

        matrixStack.scale(0.1f, 0.1f, 0.1f);
        // billboarding
        matrixStack.mulPose(entityRenderDispatcher.cameraOrientation());
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));

        PoseStack.Pose entry = matrixStack.last();
        Matrix4f positionMatrix = entry.pose();
        Matrix3f normalMatrix = entry.normal();

        VertexConsumer builder = render.getBuffer(RenderType.entityCutout(getTextureLocation(bullet)));

        addVertex(builder, positionMatrix, normalMatrix, -1, -1, 0, 0, 1, 0, 0, 1, packedLight);
        addVertex(builder, positionMatrix, normalMatrix,  1, -1, 0, 1, 1, 0, 0, 1, packedLight);
        addVertex(builder, positionMatrix, normalMatrix,  1,  1, 0, 1, 0, 0, 0, 1, packedLight);
        addVertex(builder, positionMatrix, normalMatrix, -1,  1, 0, 0, 0, 0, 0, 1, packedLight);

        matrixStack.popPose();
    }

    void addVertex(VertexConsumer builder, Matrix4f positionMatrix, Matrix3f normalMatrix, float x, float y, float z, float u, float v, float nx, float ny, float nz, int packedLight) {
        builder.vertex(positionMatrix, x, y, z)
               .color(255, 255, 255, 255)
               .uv(u, v)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(packedLight)
               .normal(normalMatrix, nx, ny, nz)
               .endVertex();
    }
}
