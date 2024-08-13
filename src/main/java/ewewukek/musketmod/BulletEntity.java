package ewewukek.musketmod;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
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
import net.minecraft.sounds.SoundEvent;
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

    public static final double DAMAGE_SPEED_THRESHOLD = 1.0;
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

    public BulletEntity(EntityType<BulletEntity>entityType, Level level) {
        super(entityType, level);
    }

    public BulletEntity(Level level) {
        this(ENTITY_TYPE, level);
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

    public float calculateEnergyFraction() {
        double maxEnergy = Math.pow(entityData.get(INITIAL_SPEED), 2);
        double energy = getDeltaMovement().lengthSqr();
        if (maxEnergy < energy) maxEnergy = energy; // empty entityData
        return (float)(energy / maxEnergy);
    }

    public int calculateParticleCount() {
        float count = entityData.get(PARTICLE_COUNT) * calculateEnergyFraction();
        float prob = count % 1;
        return (int)count + (random.nextFloat() < prob ? 1 : 0);
    }

    public DamageSource getDamageSource() {
        Entity attacker = getOwner() != null ? getOwner() : this;
        return level().damageSources().source(BULLET_DAMAGE, this, attacker);
    }

    public void setVelocity(float bulletSpeed, Vec3 direction) {
        float tickSpeed = bulletSpeed / 20; // to blocks per tick
        setInitialSpeed(tickSpeed);
        setDeltaMovement(direction.scale(tickSpeed));
    }

    public void setDamage(float bulletSpeed, float damageMin, float damageMax) {
        float tickSpeed = bulletSpeed / 20; // to blocks per tick
        float maxEnergy = tickSpeed * tickSpeed;
        float damageMultiplierMin = damageMin / maxEnergy;
        float damageMultiplierMax = damageMax / maxEnergy;
        float t = random.nextFloat();
        damageMultiplier = t * damageMultiplierMin + (1 - t) * damageMultiplierMax;
    }

    @Override
    public void tick() {
        if (++tickCounter > LIFETIME || distanceTravelled > Config.bulletMaxDistance) {
            discard();
            return;
        }

        Level level = level();

        Vec3 velocity = getDeltaMovement();
        Vec3 from = position();
        Vec3 to = from.add(velocity);

        Vec3 waterPos = Vec3.ZERO;
        wasTouchingWater = updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 0);
        if (wasTouchingWater) {
            waterPos = from;
            velocity = velocity.scale(WATER_FRICTION);
            to = from.add(velocity);
            setDeltaMovement(velocity);
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
                    double speed = velocity.length();
                    double timeInWater = 1 - distanceToFluid / speed;
                    double newSpeed = speed * (1 - timeInWater + timeInWater * Math.pow(WATER_FRICTION, timeInWater));

                    if (hitResult.getType() != HitResult.Type.MISS) {
                        if (distanceToFluid < distanceToHit) {
                            if (distanceToHit < newSpeed) {
                                timeInWater = (distanceToHit - distanceToFluid) / speed;
                                newSpeed = speed * (1 - timeInWater + timeInWater * Math.pow(WATER_FRICTION, timeInWater));
                            } else {
                                hitResult = BlockHitResult.miss(null, null, null);
                            }
                        } else {
                            fluidHitResult = BlockHitResult.miss(null, null, null);
                        }
                    }
                    velocity = velocity.scale(newSpeed / speed);
                    to = from.add(velocity);
                    setDeltaMovement(velocity);

                    if (level.isClientSide && fluidHitResult.getType() != HitResult.Type.MISS) {
                        double yv = fluidHitResult.getDirection() == Direction.UP ? 0.02 : 0;
                        createHitParticles(ParticleTypes.SPLASH, waterPos, new Vec3(0.0, yv, 0.0));
                        playHitSound(Sounds.BULLET_WATER_HIT, waterPos);
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
                if (hitResult.getType() == HitResult.Type.BLOCK && velocity.length() > DAMAGE_SPEED_THRESHOLD) {
                    BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
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
                Vec3 pos = hitResult.getLocation();
                BlockState blockState = level.getBlockState(((BlockHitResult)hitResult).getBlockPos());
                BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
                createHitParticles(particle, pos, Vec3.ZERO);
                playHitSound(blockState.getSoundType().getBreakSound(), pos);
                discard();
            }
        } else if (level.isClientSide && !wasTouchingWater && getBulletType() == BulletType.BULLET) {
            AABB aabbSelection = getBoundingBox().expandTowards(velocity).inflate(8.0);
            double length = velocity.length();
            Vec3 dir = velocity.scale(1.0 / length);
            float volume = calculateEnergyFraction();
            Predicate<Entity> predicate = entity -> (entity instanceof Player) && !entity.equals(getOwner());
            for (Entity entity : level().getEntities(this, aabbSelection, predicate)) {
                Vec3 pos = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
                Vec3 diff = pos.subtract(from);
                double proj = dir.dot(diff);
                if (proj > 0 && proj < length) {
                    Vec3 projPos = from.add(dir.scale(proj));
                    level().playLocalSound(
                        projPos.x, projPos.y, projPos.z,
                        Sounds.BULLET_FLY_BY, getSoundSource(),
                        volume, 0.92f + 0.16f * random.nextFloat(), false
                    );
                }
            }
        }

        if (level.isClientSide && wasTouchingWater) {
            double length = velocity.length();
            Vec3 step = velocity.scale(1 / length);
            Vec3 pos = waterPos.add(step.scale(0.5));
            float prob = 1.5f * calculateParticleCount() / GunItem.PARTICLE_COUNT;
            while (length > 0.5) {
                pos = pos.add(step);
                length -= 1;
                if (random.nextFloat() < prob) {
                    level.addParticle(ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, 0, 0, 0);
                }
            }
        }

        if (!wasTouchingWater) velocity = velocity.scale(AIR_FRICTION);
        setDeltaMovement(velocity.subtract(0, GRAVITY, 0));
        setPos(to);
        distanceTravelled += to.subtract(from).length();
        checkInsideBlocks();
    }

    @Override
    public void onHitEntity(EntityHitResult hitResult) {
        if (getDeltaMovement().length() < DAMAGE_SPEED_THRESHOLD) {
            return;
        }

        float damageMult = 1.0f;
        Entity target = hitResult.getEntity();
        Entity shooter = getOwner();
        if (shooter instanceof Player playerShooter) {
            if (target instanceof Player playerTarget) {
                damageMult = Config.pvpDamageMultiplier;
                if (!playerShooter.canHarmPlayer(playerTarget)) {
                    return;
                }
            }
        } else {
            damageMult = Config.mobDamageMultiplier;
        }

        DamageSource source = getDamageSource();
        float damage = calculateDamage() * damageMult;

        switch (getBulletType()) {
        case BULLET:
            int oldInvulnerableTime = target.invulnerableTime;
            if (ignoreInvulnerableTime) target.invulnerableTime = 0;
            boolean beenHurt = target.hurt(source, damage);
            if (ignoreInvulnerableTime && !beenHurt) target.invulnerableTime = oldInvulnerableTime;
            break;
        case PELLET:
            // replacing invulnerableTime works for pellets too
            // but causes hurt sound to play for each pellet hit
            DeferredDamage.hurt(target, source, damage);
            break;
        }
    }

    public EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        Entity resultEntity = null;
        Vec3 resultPos = null;
        double resultDist = 0;

        AABB aabbSelection = getBoundingBox().expandTowards(getDeltaMovement()).inflate(0.5);
        for (Entity entity : level().getEntities(this, aabbSelection, this::canHitEntity)) {
            AABB aabb = entity.getBoundingBox();
            Optional<Vec3> clipResult = aabb.clip(start, end);
            if (!clipResult.isPresent()) {
                aabb = aabb.move( // previous tick position
                    entity.xOld - entity.getX(),
                    entity.yOld - entity.getY(),
                    entity.zOld - entity.getZ()
                );
                clipResult = aabb.clip(start, end);
            }
            if (clipResult.isPresent()) {
                double dist = start.distanceToSqr(clipResult.get());
                if (dist < resultDist || resultEntity == null) {
                    resultEntity = entity;
                    resultPos = clipResult.get();
                    resultDist = dist;
                }
            }
        }

        return resultEntity != null ? new EntityHitResult(resultEntity, resultPos) : null;
    }

    public void createHitParticles(ParticleOptions particle, Vec3 position, Vec3 velocity) {
        int count = calculateParticleCount();
        for (int i = 0; i < count; i++) {
            level().addParticle(
                particle,
                position.x, position.y, position.z,
                velocity.x + 0.01 * random.nextGaussian(),
                velocity.y + 0.01 * random.nextGaussian(),
                velocity.z + 0.01 * random.nextGaussian()
            );
        }
    }

    public void playHitSound(SoundEvent sound, Vec3 position) {
        if (getBulletType() == BulletType.BULLET) {
            level().playLocalSound(
                position.x, position.y, position.z,
                sound, getSoundSource(),
                calculateEnergyFraction(),
                0.95f + 0.1f * random.nextFloat(), false
            );
        }
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
            entity.getLastSentMovement().scale(3.9 / entityData.get(INITIAL_SPEED)),
            0
        );
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        Vec3 packetVelocity = new Vec3(packet.getXa(), packet.getYa(), packet.getZa());
        setDeltaMovement(packetVelocity.scale(1.0 / 3.9));
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
