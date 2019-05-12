package ewewukek.musketmod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;

public class EntityBullet extends Entity {

    @ObjectHolder(MusketMod.MODID + ":bullet")
    public static EntityType<EntityBullet> TYPE;

    public EntityBullet(World world) {
        super(TYPE, world);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = getBoundingBox().getAverageEdgeLength() * 4;
        if (Double.isNaN(d0)) d0 = 4;
        d0 = d0 * 64;
        return distance < d0 * d0;
    }

    protected float getGravityVelocity() {
        return 0;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void registerData() {}

    @Override
    protected void readAdditional(NBTTagCompound compound) {}

    @Override
    protected void writeAdditional(NBTTagCompound compound) {}
}
