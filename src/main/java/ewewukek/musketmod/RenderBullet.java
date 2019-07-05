package ewewukek.musketmod;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBullet extends EntityRenderer<EntityBullet> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(MusketMod.MODID + ":textures/entity/bullet.png");

    public RenderBullet(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBullet arg0) {
        return TEXTURE;
    }

    @Override
    public void doRender(EntityBullet bullet, double x, double y, double z, float yaw, float partialTicks) {
        bindEntityTexture(bullet);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();

        GlStateManager.translatef((float)x, (float)y, (float)z);
        GlStateManager.scalef(0.1f, 0.1f, 0.1f);
        GlStateManager.rotatef(-renderManager.playerViewY, 0, 1, 0);
        float sign = renderManager.options.thirdPersonView == 2 ? -1 : 1;
        GlStateManager.rotatef(sign * renderManager.playerViewX, 1, 0, 0);
        GlStateManager.rotatef(180, 0, 1, 0);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GlStateManager.color4f(1, 1, 1, 1);

        GlStateManager.normal3f(0, 0, 1);
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-1, -1, 0).tex(0, 1).endVertex();
        bufferbuilder.pos( 1, -1, 0).tex(1, 1).endVertex();
        bufferbuilder.pos( 1,  1, 0).tex(1, 0).endVertex();
        bufferbuilder.pos(-1,  1, 0).tex(0, 0).endVertex();
        tessellator.draw();

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
