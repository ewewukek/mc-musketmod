package ewewukek.musketmod;

public class ScopedMusketItem extends MusketItem {
    // for client-side logic
    public static boolean isScoping;

    public ScopedMusketItem(Properties properties) {
        super(properties);
    }

    @Override
    public float bulletStdDev() {
        return Config.scopedMusketBulletStdDev;
    }

    @Override
    public int hitDurabilityDamage() {
        return 2;
    }
}
