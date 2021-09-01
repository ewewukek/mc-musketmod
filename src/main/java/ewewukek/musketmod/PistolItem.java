package ewewukek.musketmod;

import net.minecraft.util.SoundEvent;

public class PistolItem extends GunItem {
    public static final int DURABILITY = 150;

    public static float bulletStdDev;
    public static float bulletSpeed;
    public static float damageMultiplierMin;
    public static float damageMultiplierMax;

    public PistolItem(Properties properties) {
        super(properties.defaultMaxDamage(DURABILITY));
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
        return MusketMod.SOUND_PISTOL_FIRE;
    }

    @Override
    public boolean twoHanded() {
        return false;
    }
}

