package ewewukek.musketmod;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class RenderBullet extends Render<EntityBullet> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(MusketMod.MODID + ":textures/entity/bullet.png");

    public RenderBullet(RenderManager manager) {
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
        GlStateManager.scalef(0.2f, 0.2f, 0.2f);
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
