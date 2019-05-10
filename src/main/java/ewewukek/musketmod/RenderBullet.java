package ewewukek.musketmod;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class RenderBullet extends Render<EntityBullet> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(MusketMod.MODID + ":textures/entity/bullet");

    public RenderBullet(RenderManager manager) {
        super(manager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBullet arg0) {
        System.out.println("getEntityTexture");
        return TEXTURE;
    }

    @Override
    public void doRender(EntityBullet bullet, double x, double y, double z, float yaw, float partialTicks) {
        System.out.println(x + "\t" + y + "\t" + z + "\t" + yaw + "\t" + partialTicks);
    }
}
