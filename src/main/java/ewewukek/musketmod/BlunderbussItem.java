package ewewukek.musketmod;

import net.minecraft.sounds.SoundEvent;

public class BlunderbussItem extends GunItem {
    public BlunderbussItem(Properties properties) {
        super(properties);
    }

    @Override
    public float bulletStdDev() {
        return Config.blunderbussBulletStdDev;
    }

    @Override
    public float bulletSpeed() {
        return Config.blunderbussBulletSpeed;
    }

    @Override
    public int pelletCount() {
        return Config.blunderbussPelletCount;
    }

    @Override
    public BulletType bulletType() {
        return BulletType.PELLET;
    }

    @Override
    public float damageMin() {
        return Config.blunderbussDamageMin;
    }

    @Override
    public float damageMax() {
        return Config.blunderbussDamageMax;
    }

    @Override
    public SoundEvent fireSound() {
        return Sounds.BLUNDERBUSS_FIRE;
    }

    @Override
    public boolean twoHanded() {
        return true;
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
