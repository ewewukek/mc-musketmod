package ewewukek.musketmod;

import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class BulletEntity extends ThrowableEntity implements IEntityAdditionalSpawnData {
    public static final double MIN_DAMAGE = 0.5f;
    public static final double GRAVITY = 0.05;
    public static final double AIR_FRICTION = 0.99;
    public static final double WATER_FRICTION = 0.6;
    public static final short LIFETIME = 50;

    public static double maxDistance;

    public float damageMultiplier;
    public float distanceTravelled;
    public short tickCounter;

    public BulletEntity(World world) {
        super(MusketMod.BULLET_ENTITY_TYPE, world);
    }

    public BulletEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        this(world);
    }

    public boolean isFirstTick() {
        return tickCounter == 0;
    }

    // temporary adapter until mappings are updated
    public Entity getShooter() {
        return super.func_234616_v_();
    }

    public DamageSource causeMusketDamage(BulletEntity bullet, Entity attacker) {
        return (new IndirectEntityDamageSource("musket", bullet, attacker)).setProjectile();
    }

    @Override
    public void tick() {
        if (!world.isRemote && processCollision()) {
            remove();
            return;
        }

        if (++tickCounter >= LIFETIME || distanceTravelled > maxDistance) {
            remove();
            return;
        }

        Vector3d motion = getMotion();
        double posX = getPosX() + motion.x;
        double posY = getPosY() + motion.y;
        double posZ = getPosZ() + motion.z;
        distanceTravelled += motion.length();

        motion = motion.subtract(0, GRAVITY, 0);

        double friction = AIR_FRICTION;
        if (isInWater()) {
            final int count = 4;
            for (int i = 0; i != count; ++i) {
                double t = (i + 1.0) / count;
                world.addParticle(
                    ParticleTypes.BUBBLE,
                    posX - motion.x * t,
                    posY - motion.y * t,
                    posZ - motion.z * t,
                    motion.x,
                    motion.y,
                    motion.z
                );
            }
            friction = WATER_FRICTION;
        }

        setMotion(motion.scale(friction));
        setPosition(posX, posY, posZ);
        doBlockCollisions();
    }

    public boolean processCollision() {
        Vector3d from = getPositionVec();
        Vector3d to = from.add(getMotion());

        BlockRayTraceResult collision = world.rayTraceBlocks(
            new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));

        // prevents hitting entities behind an obstacle
        if (collision.getType() != RayTraceResult.Type.MISS) {
            to = collision.getHitVec();
        }

        Entity target = closestEntityOnPath(from, to);
        if (target != null) {
            if (target instanceof PlayerEntity) {
                Entity shooter = getShooter();
                if (shooter instanceof PlayerEntity && !((PlayerEntity)shooter).canAttackPlayer((PlayerEntity)target)) {

                    target = null;
                }
            }
            if (target != null) {
                hitEntity(target);
                return true;
            }
        }

        if (collision.getType() != RayTraceResult.Type.BLOCK) return false;

        BlockState blockstate = world.getBlockState(collision.getPos());
        blockstate.onProjectileCollision(world, blockstate, collision, this);

        int impactParticleCount = (int)(getMotion().lengthSquared() / 20);
        if (impactParticleCount > 0) {
            ((ServerWorld)world).spawnParticle(
                new BlockParticleData(ParticleTypes.BLOCK, blockstate),
                to.x, to.y, to.z,
                impactParticleCount,
                0, 0, 0, 0.01
            );
        }

        return true;
    }

    public void hitEntity(Entity target) {
        Entity shooter = getShooter();
        DamageSource damagesource = causeMusketDamage(this, shooter != null ? shooter : this);

        float damage = damageMultiplier * (float)getMotion().lengthSquared();
        if (damage > MIN_DAMAGE) target.attackEntityFrom(damagesource, damage);
    }

    public Predicate<Entity> getTargetPredicate() {
        Entity shooter = getShooter();
        return (entity) -> {
            return !entity.isSpectator() && entity.isAlive() && entity.canBeCollidedWith() && entity != shooter;
        };
    }

    public Entity closestEntityOnPath(Vector3d start, Vector3d end) {
        Vector3d motion = getMotion();

        Entity result = null;
        double result_dist = 0;

        AxisAlignedBB aabbSelection = getBoundingBox().expand(motion).grow(0.5);
        for (Entity entity : world.getEntitiesInAABBexcluding(this, aabbSelection, getTargetPredicate())) {
            AxisAlignedBB aabb = entity.getBoundingBox();
            Optional<Vector3d> optional = aabb.rayTrace(start, end);
            if (!optional.isPresent()) {
                aabb = aabb.offset( // previous tick position
                    entity.lastTickPosX - entity.getPosX(),
                    entity.lastTickPosY - entity.getPosY(),
                    entity.lastTickPosZ - entity.getPosZ()
                );
                optional = aabb.rayTrace(start, end);
            }
            if (optional.isPresent()) {
                double dist = start.squareDistanceTo(optional.get());
                if (dist < result_dist || result == null) {
                    result = entity;
                    result_dist = dist;
                }
            }
        }

        return result;
    }

    @Override
    protected void registerData() {}

    @Override
    protected void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        damageMultiplier = compound.getFloat("damageMultiplier");
        distanceTravelled = compound.getFloat("distanceTravelled");
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putFloat("damageMultiplier", damageMultiplier);
        compound.putFloat("distanceTravelled", distanceTravelled);
    }

// Forge {
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer data) {
        data.writeShort(0);
        Vector3d motion = getMotion();
        data.writeFloat((float)motion.x);
        data.writeFloat((float)motion.y);
        data.writeFloat((float)motion.z);
    }

    @Override
    public void readSpawnData(PacketBuffer data) {
        data.readShort();
        Vector3d motion = new Vector3d(data.readFloat(), data.readFloat(), data.readFloat());
        setMotion(motion);
    }
// }
}
