package ewewukek.musketmod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BulletRenderer extends EntityRenderer<BulletEntity> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "textures/entity/bullet.png");
    public static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BulletEntity bullet) {
        return TEXTURE;
    }

    @Override
    public void render(BulletEntity bullet, float yaw, float dt, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (bullet.isFirstTick()) return;

        poseStack.pushPose();

        switch(bullet.getBulletType()) {
        case BULLET:
            poseStack.scale(0.1f, 0.1f, 0.1f);
            break;
        case PELLET:
            poseStack.scale(0.05f, 0.05f, 0.05f);
            break;
        }

        // billboarding
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer builder = bufferSource.getBuffer(RENDER_TYPE);

        addVertex(builder, pose, -1, -1, 0, 0, 1, 0, 0, 1, light);
        addVertex(builder, pose,  1, -1, 0, 1, 1, 0, 0, 1, light);
        addVertex(builder, pose,  1,  1, 0, 1, 0, 0, 0, 1, light);
        addVertex(builder, pose, -1,  1, 0, 0, 0, 0, 0, 1, light);

        poseStack.popPose();

        super.render(bullet, yaw, dt, poseStack, bufferSource, light);
    }

    void addVertex(VertexConsumer builder, PoseStack.Pose pose, float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {
        builder.addVertex(pose, x, y, z)
               .setColor(255, 255, 255, 255)
               .setUv(u, v)
               .setOverlay(OverlayTexture.NO_OVERLAY)
               .setLight(light)
               .setNormal(pose, nx, ny, nz);
    }
}
