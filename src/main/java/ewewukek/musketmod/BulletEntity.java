package ewewukek.musketmod;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

public class BulletEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final Random random = new Random();
    static final double GRAVITY = 0.05;
    static final double AIR_FRICTION = 0.99;
    static final double WATER_FRICTION = 0.6;

    public static float damageFactorMin;
    public static float damageFactorMax;

    public UUID shooterUuid;
    public short ticksLeft;
    public boolean doFireParticles;

    @ObjectHolder(MusketMod.MODID + ":bullet")
    public static EntityType<BulletEntity> TYPE;

    public BulletEntity(World world) {
        super(TYPE, world);
        ticksLeft = 50;
    }

    public BulletEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        this(world);
    }

    public Entity getShooter() {
        return shooterUuid != null
            && world instanceof ServerWorld ? ((ServerWorld)world).getEntityByUuid(shooterUuid) : null;
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

        if (world.isRemote && doFireParticles) {
            fireParticles();
            doFireParticles = false;
        }

        if (--ticksLeft <= 0) {
            remove();
            return;
        }

        Vec3d motion = getMotion();

        double posX = getPosX() + motion.x;
        double posY = getPosY() + motion.y;
        double posZ = getPosZ() + motion.z;

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

        // copied from EntityAbstractArrow
        setPosition(posX, posY, posZ);
        doBlockCollisions();
    }

    private void fireParticles() {
        Vec3d pos = new Vec3d(getPosX(), getPosY(), getPosZ());
        Vec3d front = getMotion().normalize();

        for (int i = 0; i != 10; ++i) {
            double t = Math.pow(random.nextFloat(), 1.5);
            Vec3d p = pos.add(front.scale(1.25 + t));
            Vec3d v = front.scale(0.1).scale(1 - t);
            world.addParticle(ParticleTypes.POOF, p.x, p.y, p.z, v.x, v.y, v.z);
        }
    }

    private boolean processCollision() {
        Vec3d from = new Vec3d(getPosX(), getPosY(), getPosZ());
        Vec3d to = from.add(getMotion());

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

        if (collision.getType() == RayTraceResult.Type.MISS) return false;

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

    private void hitEntity(Entity target) {
        Entity shooter = getShooter();
        DamageSource damagesource = causeMusketDamage(this, shooter != null ? shooter : this);

        float energy = (float)getMotion().lengthSquared();
        float factor = damageFactorMin + random.nextFloat() * (damageFactorMax - damageFactorMin);
        target.attackEntityFrom(damagesource, energy * factor);
    }

    private Predicate<Entity> getTargetPredicate() {
        Entity shooter = getShooter();
        return (entity) -> {
            return !entity.isSpectator() && entity.isAlive() && entity.canBeCollidedWith() && entity != shooter;
        };
    }

    private Entity closestEntityOnPath(Vec3d start, Vec3d end) {
        Vec3d motion = getMotion();
        Entity shooter = getShooter();

        Entity result = null;
        double result_dist = motion.length() + 0.05;

        AxisAlignedBB aabbSelection = getBoundingBox().expand(motion.x, motion.y, motion.z).grow(0.5);
        for (Entity entity : world.getEntitiesInAABBexcluding(this, aabbSelection, getTargetPredicate())) {
            if (entity != shooter) {
                AxisAlignedBB aabb = entity.getBoundingBox();
                Optional<Vec3d> optional = aabb.rayTrace(start, end);
                if (optional.isPresent()) {
                    double dist = start.squareDistanceTo(optional.get());
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
    protected void readAdditional(CompoundNBT compound) {
        if (compound.hasUniqueId("shooterUuid")) {
            shooterUuid = compound.getUniqueId("shooterUuid");
        }
        ticksLeft = compound.getShort("ticksLeft");
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        if (shooterUuid != null) {
            compound.putUniqueId("shooterUuid", shooterUuid);
        }
        compound.putShort("ticksLeft", ticksLeft);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private static final UUID EMPTY_UUID = new UUID(0, 0);

    @Override
    public void writeSpawnData(PacketBuffer data) {
        data.writeUniqueId(shooterUuid != null ? shooterUuid : EMPTY_UUID);
        data.writeShort(ticksLeft);
        data.writeByte(doFireParticles ? 1 : 0);
        Vec3d motion = getMotion();
        data.writeFloat((float)motion.x);
        data.writeFloat((float)motion.y);
        data.writeFloat((float)motion.z);
    }

    @Override
    public void readSpawnData(PacketBuffer data) {
        UUID uuid = data.readUniqueId();
        if (!uuid.equals(EMPTY_UUID)) shooterUuid = uuid;
        ticksLeft = data.readShort();
        doFireParticles = data.readByte() != 0 ? true : false;
        Vec3d motion = new Vec3d(data.readFloat(), data.readFloat(), data.readFloat());
        setMotion(motion);
    }
}
