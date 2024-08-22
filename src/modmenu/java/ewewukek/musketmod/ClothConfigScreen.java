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
            Component.translatable("musketmod.options.bullet_travel_distance"), Config.bulletMaxDistance)
            .setTooltip(Component.translatable("musketmod.options.unit.blocks"))
            .setSaveConsumer(value -> Config.bulletMaxDistance = value)
            .setMin(0.0f)
            .setDefaultValue(Config.BULLET_MAX_DISTANCE)
            .build());

        commonCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.added_random_damage"), Config.randomDamage)
            .setSaveConsumer(value -> Config.randomDamage = value)
            .setMin(0.0f)
            .setDefaultValue(Config.RANDOM_DAMAGE)
            .build());

        commonCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.loading_stages_number"), Config.loadingStages)
            .setSaveConsumer(value -> Config.loadingStages = value)
            .setMin(2)
            .setDefaultValue(Config.LOADING_STAGES)
            .build());

        commonCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.loading_stage_duration"), Config.loadingStageDuration)
            .setTooltip(Component.translatable("musketmod.options.unit.seconds"))
            .setSaveConsumer(value -> Config.loadingStageDuration = value)
            .setMin(0.25f)
            .setDefaultValue(Config.LOADING_STAGE_DURATION)
            .build());

        commonCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.pvp_damage_multiplier"), Config.pvpDamageMultiplier)
            .setSaveConsumer(value -> Config.pvpDamageMultiplier = value)
            .setMin(0.0f)
            .setDefaultValue(Config.PVP_DAMAGE_MULTIPLIER)
            .build());

        commonCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.mob_damage_multiplier"), Config.mobDamageMultiplier)
            .setSaveConsumer(value -> Config.mobDamageMultiplier = value)
            .setMin(0.0f)
            .setDefaultValue(Config.MOB_DAMAGE_MULTIPLIER)
            .build());

        commonCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.pistol_pillager_chance"), Config.pistolPillagerChance)
            .setSaveConsumer(value -> Config.pistolPillagerChance = value)
            .setMin(0.0f)
            .setDefaultValue(Config.PISTOL_PILLAGER_CHANCE)
            .build());

        commonCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.musket_skeleton_chance"), Config.musketSkeletonChance)
            .setSaveConsumer(value -> Config.musketSkeletonChance = value)
            .setMin(0.0f)
            .setDefaultValue(Config.MUSKET_SKELETON_CHANCE)
            .build());

        ConfigCategory musketCategory = builder.getOrCreateCategory(Component.translatable("item.musketmod.musket"));

        musketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_std_dev"), Config.musketBulletStdDev)
            .setTooltip(Component.translatable("musketmod.options.unit.degrees"))
            .setSaveConsumer(value -> Config.musketBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.MUSKET_BULLET_STD_DEV)
            .build());

        musketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_speed"), Config.musketBulletSpeed)
            .setTooltip(Component.translatable("musketmod.options.unit.blocks_per_second"))
            .setSaveConsumer(value -> Config.musketBulletSpeed = value)
            .setMin(1.0f)
            .setDefaultValue(Config.MUSKET_BULLET_SPEED)
            .build());

        musketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage"), Config.musketDamage)
            .setSaveConsumer(value -> Config.musketDamage = value)
            .setMin(0.5f)
            .setDefaultValue(Config.MUSKET_DAMAGE)
            .build());

        musketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.headshot_damage_multiplier"), Config.headshotDamageMultiplier)
            .setSaveConsumer(value -> Config.headshotDamageMultiplier = value)
            .setMin(1.0f)
            .setDefaultValue(Config.HEADSHOT_DAMAGE_MULTIPLIER)
            .build());

        musketCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.bayonet_damage"), Config.bayonetDamage)
            .setTooltip(Component.translatable("musketmod.options.requires_restart"))
            .setSaveConsumer(value -> Config.bayonetDamage = value)
            .setMin(1)
            .setDefaultValue(Config.BAYONET_DAMAGE)
            .build());

        musketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bayonet_attack_speed"), Config.bayonetSpeed)
            .setTooltip(Component.translatable("musketmod.options.requires_restart"))
            .setSaveConsumer(value -> Config.bayonetSpeed = value)
            .setMin(0)
            .setDefaultValue(Config.BAYONET_SPEED)
            .build());

        musketCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.durability"), Config.musketDurability)
            .setTooltip(Component.translatable("musketmod.options.requires_restart"))
            .setSaveConsumer(value -> Config.musketDurability = value)
            .setMin(1)
            .setDefaultValue(Config.MUSKET_DURABILITY)
            .build());

        ConfigCategory scopedMusketCategory = builder.getOrCreateCategory(Component.translatable("item.musketmod.musket_with_scope"));

        scopedMusketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_std_dev"), Config.scopedMusketBulletStdDev)
            .setTooltip(Component.translatable("musketmod.options.unit.degrees"))
            .setSaveConsumer(value -> Config.scopedMusketBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.SCOPED_MUSKET_BULLET_STD_DEV)
            .build());

        scopedMusketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.scope_zoom_factor"), Config.scopeZoom)
            .setSaveConsumer(value -> Config.scopeZoom = value)
            .setMin(1.0f).setMax(10.0f)
            .setDefaultValue(Config.SCOPE_ZOOM)
            .build());

        scopedMusketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_gravity_multiplier"), Config.bulletGravityMultiplier)
            .setSaveConsumer(value -> Config.bulletGravityMultiplier = value)
            .setMin(0.0f).setMax(1.0f)
            .setDefaultValue(Config.BULLET_GRAVITY_MULTIPLIER)
            .build());

        scopedMusketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage_per_power_level"), Config.damagePerPowerLevel)
            .setSaveConsumer(value -> Config.damagePerPowerLevel = value)
            .setMin(0.5f)
            .setDefaultValue(Config.DAMAGE_PER_POWER_LEVEL)
            .build());

        scopedMusketCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.durability"), Config.scopedMusketDurability)
            .setTooltip(Component.translatable("musketmod.options.requires_restart"))
            .setSaveConsumer(value -> Config.scopedMusketDurability = value)
            .setMin(1)
            .setDefaultValue(Config.SCOPED_MUSKET_DURABILITY)
            .build());

        ConfigCategory blunderbussCategory = builder.getOrCreateCategory(Component.translatable("item.musketmod.blunderbuss"));

        blunderbussCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.pellet_std_dev"), Config.blunderbussBulletStdDev)
            .setTooltip(Component.translatable("musketmod.options.unit.degrees"))
            .setSaveConsumer(value -> Config.blunderbussBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.BLUNDERBUSS_BULLET_STD_DEV)
            .build());

        blunderbussCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_speed"), Config.blunderbussBulletSpeed)
            .setTooltip(Component.translatable("musketmod.options.unit.blocks_per_second"))
            .setSaveConsumer(value -> Config.blunderbussBulletSpeed = value)
            .setMin(1.0f)
            .setDefaultValue(Config.BLUNDERBUSS_BULLET_SPEED)
            .build());

        blunderbussCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage"), Config.blunderbussDamage)
            .setSaveConsumer(value -> Config.blunderbussDamage = value)
            .setMin(0.5f)
            .setDefaultValue(Config.BLUNDERBUSS_DAMAGE)
            .build());

        blunderbussCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.pellet_count"), Config.blunderbussPelletCount)
            .setSaveConsumer(value -> Config.blunderbussPelletCount = value)
            .setMin(1)
            .setDefaultValue(Config.BLUNDERBUSS_PELLET_COUNT)
            .build());

        blunderbussCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.durability"), Config.blunderbussDurability)
            .setTooltip(Component.translatable("musketmod.options.requires_restart"))
            .setSaveConsumer(value -> Config.blunderbussDurability = value)
            .setMin(1)
            .setDefaultValue(Config.BLUNDERBUSS_DURABILITY)
            .build());

        ConfigCategory pistolCategory = builder.getOrCreateCategory(Component.translatable("item.musketmod.pistol"));

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_std_dev"), Config.pistolBulletStdDev)
            .setTooltip(Component.translatable("musketmod.options.unit.degrees"))
            .setSaveConsumer(value -> Config.pistolBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.PISTOL_BULLET_STD_DEV)
            .build());

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_speed"), Config.pistolBulletSpeed)
            .setTooltip(Component.translatable("musketmod.options.unit.blocks_per_second"))
            .setSaveConsumer(value -> Config.pistolBulletSpeed = value)
            .setMin(1.0f)
            .setDefaultValue(Config.PISTOL_BULLET_SPEED)
            .build());

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage"), Config.pistolDamage)
            .setSaveConsumer(value -> Config.pistolDamage = value)
            .setMin(0.5f)
            .setDefaultValue(Config.PISTOL_DAMAGE)
            .build());

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.reduction_per_quick_charge_level"), Config.reductionPerQuickChargeLevel)
            .setTooltip(Component.translatable("musketmod.options.unit.seconds"))
            .setSaveConsumer(value -> Config.reductionPerQuickChargeLevel = value)
            .setMin(0.1f)
            .setDefaultValue(Config.REDUCTION_PER_QUICK_CHARGE_LEVEL)
            .build());

        pistolCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.durability"), Config.pistolDurability)
            .setTooltip(Component.translatable("musketmod.options.requires_restart"))
            .setSaveConsumer(value -> Config.pistolDurability = value)
            .setMin(1)
            .setDefaultValue(Config.PISTOL_DURABILITY)
            .build());

        ConfigCategory dispenserCategory = builder.getOrCreateCategory(Component.translatable("block.minecraft.dispenser"));

        dispenserCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_std_dev"), Config.dispenserBulletStdDev)
            .setTooltip(Component.translatable("musketmod.options.unit.degrees"))
            .setSaveConsumer(value -> Config.dispenserBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.DISPENSER_BULLET_STD_DEV)
            .build());

        dispenserCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_speed"), Config.dispenserBulletSpeed)
            .setTooltip(Component.translatable("musketmod.options.unit.blocks_per_second"))
            .setSaveConsumer(value -> Config.dispenserBulletSpeed = value)
            .setMin(1.0f)
            .setDefaultValue(Config.DISPENSER_BULLET_SPEED)
            .build());

        dispenserCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage"), Config.dispenserDamage)
            .setSaveConsumer(value -> Config.dispenserDamage = value)
            .setMin(0.5f)
            .setDefaultValue(Config.DISPENSER_DAMAGE)
            .build());

        builder.setSavingRunnable(() -> {
            Config.save();
        });

        return builder.build();
    }
}
