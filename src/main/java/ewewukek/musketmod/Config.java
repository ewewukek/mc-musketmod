package ewewukek.musketmod;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(modid = MusketMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    public static final Config INSTANCE;
    public static final ForgeConfigSpec SPEC;

    public final ForgeConfigSpec.ConfigValue<Double> bulletStdDev;
    public final ForgeConfigSpec.ConfigValue<Double> bulletSpeed;
    public final ForgeConfigSpec.ConfigValue<Double> damageMin;
    public final ForgeConfigSpec.ConfigValue<Double> damageMax;

    public Config(final ForgeConfigSpec.Builder builder) {
        builder.push("musket");
        bulletStdDev = builder
            .comment("Standard deviation of bullet spread (in degrees)")
            .define("bulletStdDev", 1.0);
        bulletSpeed = builder
            .comment("Muzzle velocity of bullet (in blocks per second)")
            .define("bulletSpeed", 180.0);
        damageMin = builder
            .comment("Minimum damage at point-blank range")
            .define("damageMin", 20.5);
        damageMax = builder
            .comment("Maximum damage at point-blank range")
            .define("damageMax", 21.5);
        builder.pop();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == SPEC) {
            MusketItem.bulletStdDev = (float)Math.toRadians(INSTANCE.bulletStdDev.get());
            MusketItem.bulletSpeed = INSTANCE.bulletSpeed.get() / 20.0;
            double maxEnergy = MusketItem.bulletSpeed * MusketItem.bulletSpeed;
            BulletEntity.damageFactorMin = (float)(INSTANCE.damageMin.get() / maxEnergy);
            BulletEntity.damageFactorMax = (float)(INSTANCE.damageMax.get() / maxEnergy);
        }
    }

    static {
        final Pair<Config, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Config::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }
}
