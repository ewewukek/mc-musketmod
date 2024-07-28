package ewewukek.musketmod;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

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
    public float damage() {
        return Config.pistolDamage;
    }

    @Override
    public SoundEvent fireSound(ItemStack stack) {
        return Sounds.PISTOL_FIRE;
    }

    @Override
    public boolean twoHanded() {
        return false;
    }
}
