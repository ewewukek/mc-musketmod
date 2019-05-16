package ewewukek.musketmod;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceFluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;

public class EntityBullet extends Entity {
    public UUID shooter;
    public short ticksLeft;

    @ObjectHolder(MusketMod.MODID + ":bullet")
    public static EntityType<EntityBullet> TYPE;

    public EntityBullet(World world) {
        super(TYPE, world);
        ticksLeft = 50;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = getBoundingBox().getAverageEdgeLength() * 4;
        if (Double.isNaN(d0)) d0 = 4;
        d0 = d0 * 64;
        return distance < d0 * d0;
    }

    public Entity getShootingEntity() {
        return shooter != null
            && world instanceof WorldServer ? ((WorldServer)world).getEntityFromUuid(shooter) : null;
    }

    public DamageSource causeMusketDamage(EntityBullet bullet, Entity attacker) {
        return (new EntityDamageSourceIndirect("musket", bullet, attacker)).setProjectile();
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote && processCollision()) {
            remove();
            return;
        }

        if (--ticksLeft <= 0) {
            remove();
            return;
        }

        posX += motionX;
        posY += motionY;
        posZ += motionZ;

        // copied from EntityArrow
        setPosition(posX, posY, posZ);
        doBlockCollisions();
    }

    private boolean processCollision() {
        Vec3d from = new Vec3d(posX, posY, posZ);
        Vec3d to = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);

        RayTraceResult collision = world.rayTraceBlocks(from, to, RayTraceFluidMode.NEVER, true, false);

        // prevents hitting entities behind an obstacle
        if (collision != null) {
            Vec3d hitVec = collision.hitVec;
            to = new Vec3d(hitVec.x, hitVec.y, hitVec.z);
        }

        Entity target = closestEntityOnPath(from, to);
        if (target != null) {
            if (target instanceof EntityPlayer) {
                Entity shooter = getShootingEntity();
                if (shooter instanceof EntityPlayer
                    && !((EntityPlayer)shooter).canAttackPlayer((EntityPlayer)target)) {

                    target = null;
                }
            }
            if (target != null) {
                hitEntity(target);
                return true;
            }
        }

        if (collision == null) return false;

        BlockPos blockpos = collision.getBlockPos();
        IBlockState blockstate = world.getBlockState(blockpos);
        if (!blockstate.isAir(world, blockpos)) {
            blockstate.onEntityCollision(world, blockpos, this);
        }

        return true;
    }

    private void hitEntity(Entity target) {
        int damage = 10;

        Entity shooter = getShootingEntity();
        DamageSource damagesource = causeMusketDamage(this, shooter != null ? shooter : this);

        target.attackEntityFrom(damagesource, damage);
    }

    private static final Predicate<Entity> TARGETS = EntitySelectors.NOT_SPECTATING.and(EntitySelectors.IS_ALIVE.and(Entity::canBeCollidedWith));

    private Entity closestEntityOnPath(Vec3d start, Vec3d end) {
        Entity result = null;
        double result_dist = 0;

        List<Entity> list = world.getEntitiesInAABBexcluding(this, getBoundingBox().expand(motionX, motionY, motionZ).grow(0.01), TARGETS);
        Entity shooter = getShootingEntity();
        for (Entity entity : list) {
            if (entity != shooter) {
                AxisAlignedBB aabb = entity.getBoundingBox();
                RayTraceResult collision = aabb.calculateIntercept(start, end);
                if (collision != null) {
                    double dist = start.squareDistanceTo(collision.hitVec);
                    if (dist < result_dist || result == null) {
                        result = entity;
                        result_dist = dist;
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected void registerData() {}

    @Override
    protected void readAdditional(NBTTagCompound compound) {
        shooter = compound.getUniqueId("OwnerUUID");
        ticksLeft = compound.getShort("life");
    }

    @Override
    protected void writeAdditional(NBTTagCompound compound) {
        if (shooter != null) {
            compound.setUniqueId("OwnerUUID", shooter);
        }
        compound.setShort("life", ticksLeft);
    }
}
