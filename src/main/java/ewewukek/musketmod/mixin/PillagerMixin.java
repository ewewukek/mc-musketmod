package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.Config;
import ewewukek.musketmod.GunItem;
import ewewukek.musketmod.MusketMod;
import ewewukek.musketmod.RangedGunAttackGoal;
import ewewukek.musketmod.VanillaHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(Pillager.class)
abstract class PillagerMixin {
    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, MusketMod.resource("pillager_weapon"));

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void registerGoals(CallbackInfo ci) {
        Pillager pillager = (Pillager)(Object)this;
        pillager.goalSelector.addGoal(3, new RangedGunAttackGoal<>(pillager) {
            private static final double speedModifier = 1.0;
            private static final float attackRadius = 8.0f;
            private int seeTime;
            private int attackDelay;
            private int updatePathDelay;

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
                boolean walk = (dist > attackRadius || seeTime < 5) && attackDelay == 0;
                if (walk) {
                    updatePathDelay--;
                    if (updatePathDelay <= 0) {
                        boolean canRun = !isReady() && !isLoading();
                        mob.getNavigation().moveTo(target, canRun ? speedModifier : speedModifier * 0.5);
                        updatePathDelay = RangedCrossbowAttackGoal.PATHFINDING_DELAY_RANGE.sample(mob.getRandom());
                    }
                } else {
                    updatePathDelay = 0;
                    mob.getNavigation().stop();
                }

                mob.getLookControl().setLookAt(target, 30.0f, 30.0f);

                if (isReady()) {
                    if (attackDelay > 0) {
                        attackDelay--;
                    } else if (canSee) {
                        fire(2.0f);
                    }
                } else if (!walk) {
                    reload();
                }
            }

            @Override
            public void onReady() {
                attackDelay = 20 + mob.getRandom().nextInt(20);
            }

            @Override
            public void stop() {
                super.stop();
                seeTime = 0;
            }
        });
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    private void populateDefaultEquipmentSlots(CallbackInfo ci) {
        Pillager pillager = (Pillager)(Object)this;
        if (pillager.getRandom().nextFloat() < Config.pistolPillagerChance) {
            ItemStack weapon = VanillaHelper.getRandomWeapon(pillager, LOOT_TABLE);
            if (!weapon.isEmpty()) {
                pillager.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            }
        }
    }

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private void getArmPose(CallbackInfoReturnable<AbstractIllager.IllagerArmPose> ci) {
        Pillager pillager = (Pillager)(Object)this;
        if (GunItem.isHoldingGun(pillager)) {
            ci.setReturnValue(pillager.isUsingItem()
                ? AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE
                : AbstractIllager.IllagerArmPose.CROSSBOW_HOLD);
            ci.cancel();
        }
    }
}
