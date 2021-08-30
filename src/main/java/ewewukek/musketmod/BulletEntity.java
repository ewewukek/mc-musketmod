package ewewukek.musketmod;

import java.util.Optional;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BulletEntity extends AbstractHurtingProjectile {
    public static final EntityDataAccessor<Float> INITIAL_SPEED = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.FLOAT);

    public static final double GRAVITY = 0.05;
    public static final double AIR_FRICTION = 0.99;
    public static final double WATER_FRICTION = 0.6;
    public static final short LIFETIME = 50;

    public static double maxDistance;

    public float damageMultiplier;
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
        if (++tickCounter >= LIFETIME || distanceTravelled > maxDistance) {
            discard();
            return;
        }

        Vec3 from = position();
        Vec3 to = from.add(getDeltaMovement());

        HitResult hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        // prevents hitting entities behind an obstacle
        if (hitResult.getType() != HitResult.Type.MISS) {
            to = hitResult.getLocation();
        }

        EntityHitResult entityHitResult = findHitEntity(from, to);
        if (entityHitResult != null) {
            hitResult = entityHitResult;
        }

        if (hitResult != null) {
            if (!level.isClientSide) {
                onHit(hitResult);

            } else if (hitResult.getType() == HitResult.Type.BLOCK){
                int impactParticleCount = (int)(getDeltaMovement().lengthSqr() / 20);
                if (impactParticleCount > 0) {
                    BlockState blockstate = level.getBlockState(((BlockHitResult)hitResult).getBlockPos());
                    BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, blockstate);
                    Vec3 pos = hitResult.getLocation();
                    for (int i = 0; i < impactParticleCount; ++i) {
                        level.addParticle(
                            particleOption,
                            pos.x, pos.y, pos.z,
                            random.nextGaussian() * 0.01,
                            random.nextGaussian() * 0.01,
                            random.nextGaussian() * 0.01
                        );
                    }
                }
            }
            tickCounter = LIFETIME; // discard on next tick
        }

        Vec3 motion = getDeltaMovement();
        distanceTravelled += motion.length();

        double friction = AIR_FRICTION;
        if (isInWater()) {
            final int count = 4;
            for (int i = 0; i != count; ++i) {
                double t = (i + 1.0) / count;
                Vec3 pos = from.scale(1 - t).add(to.scale(t));
                level.addParticle(
                    ParticleTypes.BUBBLE,
                    pos.x,
                    pos.y,
                    pos.z,
                    motion.x,
                    motion.y,
                    motion.z
                );
            }
            friction = WATER_FRICTION;
        }
        motion = motion.scale(friction);

        setDeltaMovement(motion.subtract(0, GRAVITY, 0));
        setPos(to);
        checkInsideBlocks();
    }

    @Override
    public void onHitEntity(EntityHitResult hitResult) {
        Entity target = hitResult.getEntity();
        if (target instanceof Player) {
            Entity shooter = getOwner();
            if (shooter instanceof Player && !((Player)shooter).canHarmPlayer((Player)target)) {
                target = null;
            }
        }
        if (target != null) {
            Entity shooter = getOwner();
            DamageSource damagesource = causeMusketDamage(this, shooter != null ? shooter : this);

            float energy = (float)getDeltaMovement().lengthSqr();
            target.hurt(damagesource, energy * damageMultiplier);
        }
    }

    public EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        Vec3 motion = getDeltaMovement();

        Entity resultEntity = null;
        Vec3 resultVec = null;
        double resultDist = 0;

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
                if (dist < resultDist || resultEntity == null) {
                    resultEntity = entity;
                    resultVec = optional.get();
                    resultDist = dist;
                }
            }
        }

        return resultEntity != null ? new EntityHitResult(resultEntity, resultVec) : null;
    }

    public void setInitialSpeed(float speed) {
        entityData.set(INITIAL_SPEED, speed);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(INITIAL_SPEED, (float)0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        damageMultiplier = compound.getFloat("damageMultiplier");
        distanceTravelled = compound.getFloat("distanceTravelled");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("damageMultiplier", damageMultiplier);
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
            getDeltaMovement().scale(ClientboundAddEntityPacket.LIMIT / entityData.get(INITIAL_SPEED))
        );
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        Vec3 packet_velocity = new Vec3(packet.getXa(), packet.getYa(), packet.getZa());
        setDeltaMovement(packet_velocity.scale(1.0 / ClientboundAddEntityPacket.LIMIT));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (INITIAL_SPEED.equals(accessor) && level.isClientSide) {
            setDeltaMovement(getDeltaMovement().scale(entityData.get(INITIAL_SPEED)));
        }
    }
}
