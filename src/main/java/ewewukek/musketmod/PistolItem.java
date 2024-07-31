package ewewukek.musketmod;

import net.minecraft.sounds.SoundEvent;

public class PistolItem extends GunItem {
    public PistolItem(Properties properties) {
        super(properties.durability(Config.pistolDurability));
    }

    @Override
    public float bulletStdDev() {
        return (float)Config.pistolBulletStdDev;
    }

    @Override
    public float bulletSpeed() {
        return (float)Config.pistolBulletSpeed;
    }

    @Override
    public float damageMin() {
        return (float)Config.pistolDamageMin;
    }

    @Override
    public float damageMax() {
        return (float)Config.pistolDamageMax;
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
