package ewewukek.musketmod;

import net.minecraft.sounds.SoundEvent;

public class BlunderbussItem extends GunItem {
    public BlunderbussItem(Properties properties) {
        super(properties.durability(Config.blunderbussDurability));
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
    public boolean ignoreInvulnerableTime() {
        return true;
    }
}
