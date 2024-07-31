package ewewukek.musketmod;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigScreen {
    public static Screen build(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("musketmod.options.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory commonCategory = builder.getOrCreateCategory(Component.translatable("musketmod.options.common"));

        commonCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_max_distance"), Config.bulletMaxDistance)
            .setSaveConsumer(value -> Config.bulletMaxDistance = value)
            .setMin(0.0f)
            .setDefaultValue(Config.BULLET_MAX_DISTANCE)
            .build());

        commonCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.pvp_damage_multiplier"), Config.pvpDamageMultiplier)
            .setSaveConsumer(value -> Config.pvpDamageMultiplier = value)
            .setMin(0.0f)
            .setDefaultValue(Config.PVP_DAMAGE_MULTIPLIER)
            .build());

        ConfigCategory musketCategory = builder.getOrCreateCategory(Component.translatable("item.musketmod.musket"));

        musketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_std_dev"), Config.musketBulletStdDev)
            .setSaveConsumer(value -> Config.musketBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.MUSKET_BULLET_STD_DEV)
            .build());

        musketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_speed"), Config.musketBulletSpeed)
            .setSaveConsumer(value -> Config.musketBulletSpeed = value)
            .setMin(1.0f)
            .setDefaultValue(Config.MUSKET_BULLET_SPEED)
            .build());

        musketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage_min"), Config.musketDamageMin)
            .setSaveConsumer(value -> Config.musketDamageMin = value)
            .setMin(0.5f)
            .setDefaultValue(Config.MUSKET_DAMAGE_MIN)
            .build());

        musketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage_max"), Config.musketDamageMax)
            .setSaveConsumer(value -> Config.musketDamageMax = value)
            .setMin(0.5f)
            .setDefaultValue(Config.MUSKET_DAMAGE_MAX)
            .build());

        musketCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.durability"), Config.musketDurability)
            .setSaveConsumer(value -> Config.musketDurability = value)
            .setMin(1)
            .setDefaultValue(Config.MUSKET_DURABILITY)
            .build());

        musketCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.bayonet_damage"), Config.bayonetDamage)
            .setSaveConsumer(value -> Config.bayonetDamage = value)
            .setMin(1)
            .setDefaultValue(Config.BAYONET_DAMAGE)
            .build());

        ConfigCategory pistolCategory = builder.getOrCreateCategory(Component.translatable("item.musketmod.pistol"));

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_std_dev"), Config.pistolBulletStdDev)
            .setSaveConsumer(value -> Config.pistolBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.PISTOL_BULLET_STD_DEV)
            .build());

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_speed"), Config.pistolBulletSpeed)
            .setSaveConsumer(value -> Config.pistolBulletSpeed = value)
            .setMin(1.0f)
            .setDefaultValue(Config.PISTOL_BULLET_SPEED)
            .build());

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage_min"), Config.pistolDamageMin)
            .setSaveConsumer(value -> Config.pistolDamageMin = value)
            .setMin(0.5f)
            .setDefaultValue(Config.PISTOL_DAMAGE_MIN)
            .build());

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage_max"), Config.pistolDamageMax)
            .setSaveConsumer(value -> Config.pistolDamageMax = value)
            .setMin(0.5f)
            .setDefaultValue(Config.PISTOL_DAMAGE_MAX)
            .build());

        pistolCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.durability"), Config.pistolDurability)
            .setSaveConsumer(value -> Config.pistolDurability = value)
            .setMin(1)
            .setDefaultValue(Config.PISTOL_DURABILITY)
            .build());

        builder.setSavingRunnable(() -> {
            Config.save();
        });

        return builder.build();
    }
}
