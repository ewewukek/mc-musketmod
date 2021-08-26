package ewewukek.musketmod;

import java.util.Optional;
import java.util.Random;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BulletEntity extends AbstractHurtingProjectile {
    private static final Random random = new Random();
    public static final double GRAVITY = 0.05;
    public static final double AIR_FRICTION = 0.99;
    public static final double WATER_FRICTION = 0.6;
    public static final short LIFETIME = 50;

    public static float damageFactorMin;
    public static float damageFactorMax;
    public static double maxDistance;

    public float distanceTravelled;
    public short tickCounter;

    public BulletEntity(EntityType<BulletEntity>entityType, Level world) {
        super(entityType, world);
    }

    public BulletEntity(Level world) {
        this(MusketMod.BULLET_ENTITY_TYPE, world);
    }

    public boolean isFirstTick() {
        return tickCounter == 0;
    }

    public DamageSource causeMusketDamage(BulletEntity bullet, Entity attacker) {
        return (new IndirectEntityDamageSource("musket", bullet, attacker)).setProjectile();
    }

    @Override
    public void tick() {
        if (!level.isClientSide && processCollision()) {
            discard();
            return;
        }

        if (++tickCounter >= LIFETIME || distanceTravelled > maxDistance) {
            discard();
            return;
        }

        Vec3 motion = getDeltaMovement();
        double posX = getX() + motion.x;
        double posY = getY() + motion.y;
        double posZ = getZ() + motion.z;
        distanceTravelled += motion.length();

        motion = motion.subtract(0, GRAVITY, 0);

        double friction = AIR_FRICTION;
        if (isInWater()) {
            final int count = 4;
            for (int i = 0; i != count; ++i) {
                double t = (i + 1.0) / count;
                level.addParticle(
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

        setDeltaMovement(motion.scale(friction));
        setPos(posX, posY, posZ);
        checkInsideBlocks();
    }

    public boolean processCollision() {
        Vec3 from = position();
        Vec3 to = from.add(getDeltaMovement());

        BlockHitResult collision = level.clip(
            new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        // prevents hitting entities behind an obstacle
        if (collision.getType() != HitResult.Type.MISS) {
            to = collision.getLocation();
        }

        Entity target = closestEntityOnPath(from, to);
        if (target != null) {
            if (target instanceof Player) {
                Entity shooter = getOwner();
                if (shooter instanceof Player && !((Player)shooter).canHarmPlayer((Player)target)) {
                    target = null;
                }
            }
            if (target != null) {
                hitEntity(target);
                return true;
            }
        }

        if (collision.getType() != HitResult.Type.BLOCK) return false;

        BlockState blockstate = level.getBlockState(collision.getBlockPos());
        blockstate.onProjectileHit(level, blockstate, collision, this);

        int impactParticleCount = (int)(getDeltaMovement().lengthSqr() / 20);
        if (impactParticleCount > 0) {
            ((ServerLevel)level).sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, blockstate),
                to.x, to.y, to.z,
                impactParticleCount,
                0, 0, 0, 0.01
            );
        }

        return true;
    }

    public void hitEntity(Entity target) {
        Entity shooter = getOwner();
        DamageSource damagesource = causeMusketDamage(this, shooter != null ? shooter : this);

        float energy = (float)getDeltaMovement().lengthSqr();
        float factor = damageFactorMin + random.nextFloat() * (damageFactorMax - damageFactorMin);
        target.hurt(damagesource, energy * factor);
    }

    public Entity closestEntityOnPath(Vec3 start, Vec3 end) {
        Vec3 motion = getDeltaMovement();

        Entity result = null;
        double result_dist = 0;

        AABB aabbSelection = getBoundingBox().expandTowards(motion).inflate(0.5);
        for (Entity entity : level.getEntities(this, aabbSelection, this::canHitEntity)) {
            AABB aabb = entity.getBoundingBox();
            Optional<Vec3> optional = aabb.clip(start, end);
            if (!optional.isPresent()) {
                aabb = aabb.move( // previous tick position
                    entity.xOld - entity.getX(),
                    entity.yOld - entity.getY(),
                    entity.zOld - entity.getZ()
                );
                optional = aabb.clip(start, end);
            }
            if (optional.isPresent()) {
                double dist = start.distanceToSqr(optional.get());
                if (dist < result_dist || result == null) {
                    result = entity;
                    result_dist = dist;
                }
            }
        }

        return result;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        distanceTravelled = compound.getFloat("distanceTravelled");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("distanceTravelled", distanceTravelled);
    }

    // workaround for ClientboundAddEntityPacket.LIMIT
    @Override
    public Packet<?> getAddEntityPacket() {
        Entity owner = getOwner();
        return new ClientboundAddEntityPacket(
            getId(), getUUID(),
            getX(), getY(), getZ(),
            getXRot(), getYRot(),
            getType(), owner != null ? owner.getId() : 0,
            getDeltaMovement().scale(ClientboundAddEntityPacket.LIMIT / MusketItem.bulletSpeed)
        );
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        Vec3 packet_velocity = new Vec3(packet.getXa(), packet.getYa(), packet.getZa());
        setDeltaMovement(packet_velocity.scale(MusketItem.bulletSpeed / ClientboundAddEntityPacket.LIMIT));
    }
}
