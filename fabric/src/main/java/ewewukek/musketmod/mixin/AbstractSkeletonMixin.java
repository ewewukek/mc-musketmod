package ewewukek.musketmod.mixin;

import java.util.EnumSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ewewukek.musketmod.Config;
import ewewukek.musketmod.Items;
import ewewukek.musketmod.RangedGunAttackGoal;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractSkeleton.class)
public class AbstractSkeletonMixin {
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void registerGoals(CallbackInfo ci) {
        AbstractSkeleton skeleton = (AbstractSkeleton)(Object)this;
        skeleton.goalSelector.addGoal(4, new RangedGunAttackGoal<>(skeleton, EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)) {
            private static final double speedModifier = 1.0;
            private static final float attackRadius = 15.0f;
            private int seeTime;
            private int attackDelay;
            private boolean strafingClockwise;
            private boolean strafingBackwards;
            private int strafingTime = -1;

            @Override
            public boolean canContinueToUse() {
                return (isTargetValid() || !mob.getNavigation().isDone()) && isHoldingGun();
            }

            @Override
            public void start() {
                super.start();
                mob.setAggressive(true);
            }

            @Override
            public void stop() {
                super.stop();
                seeTime = 0;
                attackDelay = 0;
            }

            @Override
            public void tick() {
                super.tick();

                LivingEntity target = mob.getTarget();
                if (target == null) return;

                boolean canSee = mob.getSensing().hasLineOfSight(target);
                boolean wasSeeing = seeTime > 0;
                if (canSee != wasSeeing) {
                    seeTime = 0;
                }
                if (canSee) {
                    seeTime++;
                } else {
                    seeTime--;
                }

                float dist = mob.distanceTo(target);
                if (dist < attackRadius && seeTime >= 20) {
                    mob.getNavigation().stop();
                    strafingTime++;
                } else {
                    mob.getNavigation().moveTo(target, speedModifier);
                    strafingTime = -1;
                }

                if (strafingTime >= 20) {
                    if (mob.getRandom().nextFloat() < 0.3f) {
                        strafingClockwise = !strafingClockwise;
                    }
                    if (mob.getRandom().nextFloat() < 0.3f) {
                        strafingBackwards = !strafingBackwards;
                    }
                    strafingTime = 0;
                }

                if (strafingTime > -1) {
                    if (dist > 0.75f * attackRadius) {
                        strafingBackwards = false;
                    } else if (dist < 0.25f * attackRadius) {
                        strafingBackwards = true;
                    }
                    mob.getMoveControl().strafe(strafingBackwards ? -0.5f : 0.5f, strafingClockwise ? 0.5f : -0.5f);
                    if (mob.getControlledVehicle() instanceof Mob vehicle) {
                        vehicle.lookAt(target, 30.0f, 30.0f);
                    }
                    mob.lookAt(target, 30.0f, 30.0f);
                } else {
                    mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
                }

                if (seeTime < -60) {
                    attackDelay = Math.max(20, attackDelay);
                }
                if (attackDelay > 0) {
                    attackDelay--;
                } else {
                    if (isReady()) {
                        if (canSee) {
                            boolean isHard = mob.level().getDifficulty() == Difficulty.HARD;
                            fire(isHard ? 2.0f : 6.0f);
                            attackDelay = 10;
                        }
                    } else {
                        reload();
                    }
                }
            }

            @Override
            public void onReady() {
                boolean isHard = mob.level().getDifficulty() == Difficulty.HARD;
                attackDelay = Math.max(isHard ? 20 : 40, attackDelay);
            }
        });
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    private void populateDefaultEquipmentSlots(CallbackInfo ci) {
        AbstractSkeleton skeleton = (AbstractSkeleton)(Object)this;
        if (skeleton.level().getDifficulty() != Difficulty.EASY
        && skeleton.getRandom().nextFloat() < Config.musketSkeletonChance) {
            skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.MUSKET));
        }
    }

}
