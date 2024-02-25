package ewewukek.musketmod;

import net.minecraft.sounds.SoundEvent;

public class PistolItem extends GunItem {
    public static float bulletStdDev;
    public static float bulletSpeed;
    public static float damageMultiplierMin;
    public static float damageMultiplierMax;

    public static int durability;

    public PistolItem(Properties properties) {
        super(properties.defaultDurability(durability));
    }

    @Override
    public float bulletStdDev() {
        return bulletStdDev;
    }

    @Override
    public float bulletSpeed() {
        return bulletSpeed;
    }

    @Override
    public float damageMultiplierMin() {
        return damageMultiplierMin;
    }

    @Override
    public float damageMultiplierMax() {
        return damageMultiplierMax;
    }

    @Override
    public SoundEvent fireSound() {
        return Sounds.PISTOL_FIRE;
    }

    @Override
    public boolean twoHanded() {
        return false;
    }

    @Override
    public boolean ignoreInvulnerableTime() {
        return true;
    }
}
