package ewewukek.musketmod;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BulletRenderer extends EntityRenderer<BulletEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(MusketMod.MODID + ":textures/entity/bullet.png");

    public BulletRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getEntityTexture(BulletEntity bullet) {
        return TEXTURE;
    }

    // doRender ?
    @Override
    public void func_225623_a_(BulletEntity bullet, float yaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer render, int mask) {
        matrixStack.func_227860_a_(); // push

        // scale
        matrixStack.func_227862_a_(0.1f, 0.1f, 0.1f);
        // billboarding
        matrixStack.func_227863_a_(renderManager.func_229098_b_());
        matrixStack.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(180));

        MatrixStack.Entry entry = matrixStack.func_227866_c_();
        Matrix4f viewMatrix = entry.func_227870_a_();
        Matrix3f normalMatrix = entry.func_227872_b_();

        IVertexBuilder builder = render.getBuffer(RenderType.func_228638_b_(getEntityTexture(bullet)));

        addVertex(builder, viewMatrix, normalMatrix, -1, -1, 0, 0, 1, 0, 0, 1, mask);
        addVertex(builder, viewMatrix, normalMatrix,  1, -1, 0, 1, 1, 0, 0, 1, mask);
        addVertex(builder, viewMatrix, normalMatrix,  1,  1, 0, 1, 0, 0, 0, 1, mask);
        addVertex(builder, viewMatrix, normalMatrix, -1,  1, 0, 0, 0, 0, 0, 1, mask);

        matrixStack.func_227865_b_(); // pop
    }

    void addVertex(IVertexBuilder builder, Matrix4f viewMatrix, Matrix3f normalMatrix, float x, float y, float z, float u, float v, float nx, float ny, float nz, int mask) {
        builder.func_227888_a_(viewMatrix, x, y, z)
               .func_225586_a_(255, 255, 255, 255)
               .func_225583_a_(u, v)
               .func_227891_b_(OverlayTexture.field_229196_a_)
               .func_227886_a_(mask)
               .func_227887_a_(normalMatrix, nx, ny, nz)
               .endVertex();
    }
}
