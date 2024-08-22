package ewewukek.musketmod;

public class ScopedMusketItem extends MusketItem {
    public static final int RECOIL_TICKS = 12;
    public static final float RECOIL_AMOUNT = 0.25f;

    // for client-side logic
    public static boolean isScoping;
    public static int recoilTicks;

    public ScopedMusketItem(Properties properties) {
        super(properties);
    }

    @Override
    public float bulletStdDev() {
        return Config.scopedMusketBulletStdDev;
    }

    @Override
    public float bulletDropReduction() {
        return 1.0f - Config.bulletGravityMultiplier;
    }

    @Override
    public int hitDurabilityDamage() {
        return 2;
    }
}
