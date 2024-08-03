package ewewukek.musketmod;

import java.util.Optional;
import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BulletEntity extends AbstractHurtingProjectile {
    // workaround for ClientboundAddEntityPacket.LIMIT
    public static final EntityDataAccessor<Float> INITIAL_SPEED = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> PARTICLE_COUNT = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Byte> BULLET_TYPE = SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.BYTE);
    public static final ResourceKey<DamageType> BULLET_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "bullet"));
    public static EntityType<BulletEntity> ENTITY_TYPE;

    public static final double MIN_DAMAGE = 0.5;
    public static final double GRAVITY = 0.05;
    public static final double AIR_FRICTION = 0.99;
    public static final double WATER_FRICTION = 0.6;
    public static final short LIFETIME = 100;

    public float damageMultiplier;
    public boolean ignoreInvulnerableTime;
    public float distanceTravelled;
    public short tickCounter;

    private boolean packetSpeedReceived;

    public static void register(BiConsumer<String, EntityType<?>> helper) {
        EntityType.Builder<BulletEntity> builder = EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(64)
            .updateInterval(20);
        MusketMod.disableVelocityUpdate(builder);
        ENTITY_TYPE = builder.build("bullet");
        helper.accept("bullet", ENTITY_TYPE);
    }

    public BulletEntity(EntityType<BulletEntity>entityType, Level world) {
        super(entityType, world);
    }

    public BulletEntity(Level world) {
        this(ENTITY_TYPE, world);
    }

    public boolean isFirstTick() {
        return tickCounter == 0;
    }

    public void discardOnNextTick() {
        tickCounter = LIFETIME;
    }

    public float calculateDamage() {
        return damageMultiplier * (float)getDeltaMovement().lengthSqr();
    }

    public int calculateParticleCount() {
        double maxEnergy = Math.pow(entityData.get(INITIAL_SPEED), 2);
        double energy = getDeltaMovement().lengthSqr();
        if (maxEnergy < energy) maxEnergy = energy; // empty entityData
        float count = entityData.get(PARTICLE_COUNT) * (float)(energy / maxEnergy);
        float prob = count % 1;
        return (int)count + (random.nextFloat() < prob ? 1 : 0);
    }

    public DamageSource causeMusketDamage(BulletEntity bullet, Entity attacker) {
        return level().damageSources().source(BULLET_DAMAGE, bullet, attacker);
    }

    @Override
    public void tick() {
        if (++tickCounter >= LIFETIME || distanceTravelled > Config.bulletMaxDistance) {
            discard();
            return;
        }

        Level level = level();

        Vec3 motion = getDeltaMovement();
        Vec3 from = position();
        Vec3 to = from.add(motion);

        Vec3 waterPos = from;
        wasTouchingWater = updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 0);
        if (wasTouchingWater) {
            motion = motion.scale(WATER_FRICTION);
            to = from.add(motion);
            setDeltaMovement(motion);
        }

        HitResult hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        // prevents hitting entities behind an obstacle
        if (hitResult.getType() != HitResult.Type.MISS) {
            to = hitResult.getLocation();
        }

        EntityHitResult entityHitResult = findHitEntity(from, to);
        if (entityHitResult != null) {
            hitResult = entityHitResult;
            to = hitResult.getLocation();
        }

        if (!wasTouchingWater) {
            BlockHitResult fluidHitResult = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
            if (fluidHitResult.getType() == HitResult.Type.BLOCK) {
                FluidState fluid = level.getFluidState(fluidHitResult.getBlockPos());
                double distanceToFluid = fluidHitResult.getLocation().subtract(from).length();
                double distanceToHit = to.subtract(from).length();

                if (fluid.is(FluidTags.WATER)) {
                    wasTouchingWater = true;
                    waterPos = fluidHitResult.getLocation();
                    double velocity = motion.length();
                    double timeInWater = 1 - distanceToFluid / velocity;
                    double newVelocity = velocity * (1 - timeInWater + timeInWater * Math.pow(WATER_FRICTION, timeInWater));

                    if (hitResult.getType() != HitResult.Type.MISS) {
                        if (distanceToFluid < distanceToHit) {
                            if (distanceToHit < newVelocity) {
                                timeInWater = (distanceToHit - distanceToFluid) / velocity;
                                newVelocity = velocity * (1 - timeInWater + timeInWater * Math.pow(WATER_FRICTION, timeInWater));
                            } else {
                                hitResult = BlockHitResult.miss(null, null, null);
                            }
                        } else {
                            fluidHitResult = BlockHitResult.miss(null, null, null);
                        }
                    }
                    motion = motion.scale(newVelocity / velocity);
                    to = from.add(motion);
                    setDeltaMovement(motion);

                    if (fluidHitResult.getType() != HitResult.Type.MISS) {
                        int particleCount = calculateParticleCount();
                        if (particleCount > 0) {
                            Vec3 pos = fluidHitResult.getLocation();
                            double yv = fluidHitResult.getDirection() == Direction.UP ? 0.02 : 0;
                            for (int i = 0; i < particleCount; ++i) {
                                level.addParticle(
                                    ParticleTypes.SPLASH,
                                    pos.x, pos.y, pos.z,
                                    random.nextGaussian() * 0.01,
                                    random.nextGaussian() * 0.01 + yv,
                                    random.nextGaussian() * 0.01
                                );
                            }
                        }
                    }
                } else if (fluid.is(FluidTags.LAVA)) {
                    if (hitResult.getType() == HitResult.Type.MISS || distanceToFluid < distanceToHit) {
                        hitResult = fluidHitResult;
                        to = fluidHitResult.getLocation();
                    }
                }
            }
        }

        if (hitResult.getType() != HitResult.Type.MISS) {
            if (!level.isClientSide) {
                onHit(hitResult);
                if (hitResult.getType() == HitResult.Type.BLOCK && calculateDamage() > MIN_DAMAGE) {
                    BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    BlockState blockState = level().getBlockState(blockPos);
                    // should not get ignited twice
                    // since first time would remove the block
                    if (blockState.getBlock() == Blocks.TNT) {
                        TntBlock.explode(level(), blockPos);
                        level.removeBlock(blockPos, false);
                    }
                }
                discardOnNextTick();

            } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                int particleCount = calculateParticleCount();
                if (particleCount > 0) {
                    BlockState blockstate = level.getBlockState(((BlockHitResult)hitResult).getBlockPos());
                    BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, blockstate);
                    Vec3 pos = hitResult.getLocation();
                    for (int i = 0; i < particleCount; ++i) {
                        level.addParticle(
                            particleOption,
                            pos.x, pos.y, pos.z,
                            random.nextGaussian() * 0.01,
                            random.nextGaussian() * 0.01,
                            random.nextGaussian() * 0.01
                        );
                    }
                }
                discard();
            }
        }

        if (wasTouchingWater) {
            double len = motion.length();
            Vec3 step = motion.scale(1 / len);
            Vec3 pos = waterPos.add(step.scale(0.5));
            float prob = 1.5f * calculateParticleCount() / GunItem.PARTICLE_COUNT;
            while (len > 0.5) {
                pos = pos.add(step);
                len -= 1;
                if (random.nextFloat() < prob) {
                    level.addParticle(ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, 0, 0, 0);
                }
            }
        } else {
            motion = motion.scale(AIR_FRICTION);
        }

        setDeltaMovement(motion.subtract(0, GRAVITY, 0));
        setPos(to);
        distanceTravelled += to.subtract(from).length();
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
            DamageSource damageSource = causeMusketDamage(this, shooter != null ? shooter : this);

            float damage = calculateDamage();
            if ((shooter instanceof Player) && (target instanceof Player)) {
                damage *= Config.pvpDamageMultiplier;
            }
            if (damage > MIN_DAMAGE) {
                switch (getBulletType()) {
                case BULLET:
                    int oldInvulnerableTime = target.invulnerableTime;
                    if (ignoreInvulnerableTime) target.invulnerableTime = 0;
                    boolean beenHurt = target.hurt(damageSource, damage);
                    if (ignoreInvulnerableTime && !beenHurt) target.invulnerableTime = oldInvulnerableTime;
                    break;
                case PELLET:
                    // replacing invulnerableTime works for pellets too
                    // but causes hurt sound to play for each pellet hit
                    DeferredDamage.hurt(target, damageSource, damage);
                    break;
                }
            }
        }
    }

    public EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        Vec3 motion = getDeltaMovement();

        Entity resultEntity = null;
        Vec3 resultVec = null;
        double resultDist = 0;

        AABB aabbSelection = getBoundingBox().expandTowards(motion).inflate(0.5);
        for (Entity entity : level().getEntities(this, aabbSelection, this::canHitEntity)) {
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

    public void setParticleCount(float count) {
        entityData.set(PARTICLE_COUNT, count);
    }

    public BulletType getBulletType() {
        return BulletType.fromByte(entityData.get(BULLET_TYPE));
    }

    public void setBulletType(BulletType type) {
        entityData.set(BULLET_TYPE, type.toByte());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(INITIAL_SPEED, 0.0f);
        builder.define(PARTICLE_COUNT, 0.0f);
        builder.define(BULLET_TYPE, (byte)0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        damageMultiplier = compound.getFloat("damageMultiplier");
        ignoreInvulnerableTime = compound.getByte("ignoreInvulnerableTime") != 0;
        distanceTravelled = compound.getFloat("distanceTravelled");
        entityData.set(PARTICLE_COUNT, compound.getFloat("particleCount"));
        entityData.set(BULLET_TYPE, compound.getByte("bulletType"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("damageMultiplier", damageMultiplier);
        compound.putByte("ignoreInvulnerableTime", (byte)(ignoreInvulnerableTime ? 1 : 0));
        compound.putFloat("distanceTravelled", distanceTravelled);
        compound.putFloat("particleCount", entityData.get(PARTICLE_COUNT));
        compound.putByte("bulletType", entityData.get(BULLET_TYPE));
    }

    // workaround for ClientboundAddEntityPacket.LIMIT
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        Entity owner = getOwner();
        Vec3 position = entity.getPositionBase();
        return new ClientboundAddEntityPacket(
            getId(), getUUID(),
            position.x(), position.y(), position.z(),
            entity.getLastSentXRot(), entity.getLastSentYRot(),
            getType(), owner != null ? owner.getId() : 0,
            entity.getLastSentMovement().scale(4.0 / entityData.get(INITIAL_SPEED)),
            0
        );
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        Vec3 packet_velocity = new Vec3(packet.getXa(), packet.getYa(), packet.getZa());
        setDeltaMovement(packet_velocity.scale(1.0 / 4.0));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (INITIAL_SPEED.equals(accessor) && level().isClientSide && !packetSpeedReceived) {
            setDeltaMovement(getDeltaMovement().scale(entityData.get(INITIAL_SPEED)));
            packetSpeedReceived = true;
        }
    }
}
