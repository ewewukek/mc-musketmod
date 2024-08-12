package ewewukek.musketmod;

import net.minecraft.sounds.SoundEvent;

public class ScopedMusketItem extends GunItem {
    // for client-side logic
    public static boolean isScoping;

    public ScopedMusketItem(Properties properties) {
        super(properties.durability(Config.scopedMusketDurability));
    }

    @Override
    public float bulletStdDev() {
        return Config.scopedMusketBulletStdDev;
    }

    @Override
    public float bulletSpeed() {
        return Config.musketBulletSpeed;
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
        return Config.musketDamageMin;
    }

    @Override
    public float damageMax() {
        return Config.musketDamageMax;
    }

    @Override
    public SoundEvent fireSound() {
        return Sounds.MUSKET_FIRE;
    }

    @Override
    public boolean twoHanded() {
        return true;
    }

    @Override
    public int hitDurabilityDamage() {
        return 2;
    }

    @Override
    public boolean ignoreInvulnerableTime() {
        return false;
    }
}
