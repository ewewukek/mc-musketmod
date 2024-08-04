package ewewukek.musketmod;

import java.lang.reflect.Method;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;

public class RangedGunAttackGoal<T extends Monster> extends Goal {
    public final T mob;
    public final double speedModifier;
    public final float attackRadius;

    public boolean predictTargetMovement;
    public float inaccuracyStdDev;
    public int getCloserTicks = 5;
    public float lookAtXRotSpeed = 30.0f;
    public float lookAtYRotSpeed = 30.0f;
    public double loadingSpeedMultiplier = 0.3;
    public int updatePathDelayMin = 20;
    public int updatePathDelayMax = 40;
    public int attackDelayMin = 20;
    public int attackDelayMax = 40;

    private State state = State.EMPTY;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    public RangedGunAttackGoal(T mob, double speedModifier, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackRadius = attackRadius;
    }

    @Override
    public boolean canUse() {
        return isTargetValid() && isHoldingGun();
    }

    public boolean isTargetValid() {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    public boolean isHoldingGun() {
        return GunItem.isHoldingGun(mob);
    }

    public InteractionHand getGunHoldingHand() {
        return GunItem.getHoldingHand(mob);
    }

    @Override
    public void stop() {
        super.stop();
        mob.setAggressive(false);
        mob.setTarget(null);
        seeTime = 0;
        if (mob.isUsingItem()) mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        boolean canSee = mob.getSensing().hasLineOfSight(target);
        boolean wasSeeing = seeTime > 0;
        if (canSee != wasSeeing) seeTime = 0;
        if (canSee)
            seeTime++;
        else
            seeTime--;

        float dist = mob.distanceTo(target);
        boolean shouldGetCloser = (dist > attackRadius || seeTime < getCloserTicks) && attackDelay == 0;
        if (shouldGetCloser) {
            updatePathDelay--;
            if (updatePathDelay <= 0) {
                double mod = speedModifier;
                if (state == State.LOADING) mod *= loadingSpeedMultiplier;
                mob.getNavigation().moveTo(target, mod);
                float f = mob.getRandom().nextFloat();
                updatePathDelay = Math.round(f * updatePathDelayMin + (1 - f) * updatePathDelayMax);
            }
        } else {
            updatePathDelay = 0;
            mob.getNavigation().stop();
        }

        mob.getLookControl().setLookAt(target, lookAtXRotSpeed, lookAtYRotSpeed);

        InteractionHand hand = getGunHoldingHand();
        ItemStack stack = mob.getItemInHand(hand);

        switch(state) {
        case EMPTY:
            if (!shouldGetCloser) {
                GunItem.setLoadingStage(stack, 1);
                mob.startUsingItem(hand);
                state = State.LOADING;
                setGunLoading(true);
            }
            break;
        case LOADING:
            if (mob.isUsingItem()) {
                if (GunItem.isLoaded(stack)) {
                    mob.releaseUsingItem();
                    state = State.READY;
                    float f = mob.getRandom().nextFloat();
                    attackDelay = Math.round(f * attackDelayMin + (1 - f) * attackDelayMax);
                    setGunLoading(false);
                    setGunLoaded(true);
                }
            } else {
                state = State.EMPTY;
                setGunLoading(false);
            }
            break;
        case READY:
            attackDelay--;
            if (attackDelay <= 0) {
                GunItem gun = (GunItem)stack.getItem();
                gun.mobUse(mob, hand, gun.aimAt(mob, target, inaccuracyStdDev, predictTargetMovement));
                state = State.EMPTY;
                onGunAttackPerformed();
                setGunLoaded(false);
            }
        }
    }

    public void setGunLoading(boolean loading) {
        try {
            Method method = mob.getClass().getDeclaredMethod("setGunLoading", Boolean.TYPE);
            method.invoke(mob, loading);
        } catch (ReflectiveOperationException e) {}
    }

    public void setGunLoaded(boolean loaded) {
        try {
            Method method = mob.getClass().getDeclaredMethod("setGunLoaded", Boolean.TYPE);
            method.invoke(mob, loaded);
        } catch (ReflectiveOperationException e) {}
    }

    public void onGunAttackPerformed() {
        try {
            Method method = mob.getClass().getDeclaredMethod("onGunAttackPerformed");
            method.invoke(mob);
        } catch (ReflectiveOperationException e) {}
    }

    enum State {
        EMPTY,
        LOADING,
        READY;
    }
}
