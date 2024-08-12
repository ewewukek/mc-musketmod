package ewewukek.musketmod;

import net.minecraft.sounds.SoundEvent;

public class PistolItem extends GunItem {
    public PistolItem(Properties properties) {
        super(properties);
    }

    @Override
    public float bulletStdDev() {
        return Config.pistolBulletStdDev;
    }

    @Override
    public float bulletSpeed() {
        return Config.pistolBulletSpeed;
    }

    @Override
    public int pelletCount() {
        return 1;
    }

    @Override
    public BulletType bulletType() {
        return BulletType.BULLET;
    }

    @Override
    public float damageMin() {
        return Config.pistolDamageMin;
    }

    @Override
    public float damageMax() {
        return Config.pistolDamageMax;
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
    public int hitDurabilityDamage() {
        return 1;
    }

    @Override
    public boolean ignoreInvulnerableTime() {
        return true;
    }
}
