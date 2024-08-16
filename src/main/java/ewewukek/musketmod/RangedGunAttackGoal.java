package ewewukek.musketmod;

import java.util.EnumSet;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class RangedGunAttackGoal<T extends Monster> extends Goal {
    public final T mob;
    private boolean isLoading;

    public RangedGunAttackGoal(T mob) {
        this(mob, EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public RangedGunAttackGoal(T mob, EnumSet<Flag>flags) {
        this.mob = mob;
        setFlags(flags);
    }

    @Override
    public boolean canUse() {
        return isTargetValid() && canUseGun();
    }

    public boolean isTargetValid() {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    public boolean canUseGun() {
        return GunItem.isHoldingGun(mob) && !isInFluid();
    }

    private boolean isInFluid() {
        return mob.isEyeInFluid(FluidTags.WATER) || mob.isEyeInFluid(FluidTags.LAVA);
    }

    public boolean isReady() {
        InteractionHand hand = GunItem.getHoldingHand(mob);
        if (hand == null) return false;
        ItemStack stack = mob.getItemInHand(hand);
        return GunItem.isLoaded(stack);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void onReady() {
    }

    public void reload() {
        InteractionHand hand = GunItem.getHoldingHand(mob);
        if (hand == null) return;
        ItemStack stack = mob.getItemInHand(hand);
        if (!isLoading && !GunItem.isLoaded(stack)) {
            GunItem.setLoadingStage(stack, 1);
            mob.startUsingItem(hand);
            isLoading = true;
        }
    }

    public void fire(float spread) {
        InteractionHand hand = GunItem.getHoldingHand(mob);
        if (hand == null) return;
        ItemStack stack = mob.getItemInHand(hand);
        if (GunItem.isLoaded(stack)) {
            GunItem gun = (GunItem)stack.getItem();
            Vec3 direction = gun.aimAt(mob, mob.getTarget());
            if (spread > 0) {
                direction = GunItem.addUniformSpread(direction, mob.getRandom(), spread);
            }
            gun.mobUse(mob, hand, direction);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (isLoading) {
            if (mob.isUsingItem()) {
                if (GunItem.isLoaded(mob.getUseItem())) {
                    mob.releaseUsingItem();
                    isLoading = false;
                    onReady();
                }
            } else {
                isLoading = false;
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        mob.setAggressive(false);
        mob.setTarget(null);
        isLoading = false;
        if (mob.isUsingItem()) {
            mob.stopUsingItem();
        }
    }
}
