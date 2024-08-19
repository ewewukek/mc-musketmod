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

        commonCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.loading_stages"), Config.loadingStages)
            .setSaveConsumer(value -> Config.loadingStages = value)
            .setMin(2)
            .setDefaultValue(Config.LOADING_STAGES)
            .build());

        commonCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.loading_stage_duration"), Config.loadingStageDuration)
            .setTooltip(Component.translatable("musketmod.options.tooltip.seconds"))
            .setSaveConsumer(value -> Config.loadingStageDuration = value)
            .setMin(0.25f)
            .setDefaultValue(Config.LOADING_STAGE_DURATION)
            .build());

        commonCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_max_distance"), Config.bulletMaxDistance)
            .setTooltip(Component.translatable("musketmod.options.tooltip.blocks"))
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
            .setTooltip(Component.translatable("musketmod.options.tooltip.degrees"))
            .setSaveConsumer(value -> Config.musketBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.MUSKET_BULLET_STD_DEV)
            .build());

        musketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_speed"), Config.musketBulletSpeed)
            .setTooltip(Component.translatable("musketmod.options.tooltip.blocks_per_second"))
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
            .setTooltip(Component.translatable("musketmod.options.tooltip.restart"))
            .setSaveConsumer(value -> Config.musketDurability = value)
            .setMin(1)
            .setDefaultValue(Config.MUSKET_DURABILITY)
            .build());

        musketCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.bayonet_damage"), Config.bayonetDamage)
            .setTooltip(Component.translatable("musketmod.options.tooltip.restart"))
            .setSaveConsumer(value -> Config.bayonetDamage = value)
            .setMin(1)
            .setDefaultValue(Config.BAYONET_DAMAGE)
            .build());

        ConfigCategory scopedMusketCategory = builder.getOrCreateCategory(Component.translatable("item.musketmod.musket_with_scope"));

        scopedMusketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_std_dev"), Config.scopedMusketBulletStdDev)
            .setTooltip(Component.translatable("musketmod.options.tooltip.degrees"))
            .setSaveConsumer(value -> Config.scopedMusketBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.SCOPED_MUSKET_BULLET_STD_DEV)
            .build());

        scopedMusketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.scope_zoom"), Config.scopeZoom)
            .setSaveConsumer(value -> Config.scopeZoom = value)
            .setMin(1.0f).setMax(10.0f)
            .setDefaultValue(Config.SCOPE_ZOOM)
            .build());

        scopedMusketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage_per_power_level"), Config.damagePerPowerLevel)
            .setSaveConsumer(value -> Config.damagePerPowerLevel = value)
            .setMin(0.5f)
            .setDefaultValue(Config.DAMAGE_PER_POWER_LEVEL)
            .build());

        scopedMusketCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.drop_reduction_per_power_level"), Config.dropReductionPerPowerLevel)
            .setSaveConsumer(value -> Config.dropReductionPerPowerLevel = value)
            .setMin(0.0f).setMax(0.2f)
            .setDefaultValue(Config.DROP_REDUCTION_PER_POWER_LEVEL)
            .build());

        scopedMusketCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.durability"), Config.scopedMusketDurability)
            .setTooltip(Component.translatable("musketmod.options.tooltip.restart"))
            .setSaveConsumer(value -> Config.scopedMusketDurability = value)
            .setMin(1)
            .setDefaultValue(Config.SCOPED_MUSKET_DURABILITY)
            .build());

        ConfigCategory blunderbussCategory = builder.getOrCreateCategory(Component.translatable("item.musketmod.blunderbuss"));

        blunderbussCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_std_dev"), Config.blunderbussBulletStdDev)
            .setTooltip(Component.translatable("musketmod.options.tooltip.degrees"))
            .setSaveConsumer(value -> Config.blunderbussBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.BLUNDERBUSS_BULLET_STD_DEV)
            .build());

        blunderbussCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_speed"), Config.blunderbussBulletSpeed)
            .setTooltip(Component.translatable("musketmod.options.tooltip.blocks_per_second"))
            .setSaveConsumer(value -> Config.blunderbussBulletSpeed = value)
            .setMin(1.0f)
            .setDefaultValue(Config.BLUNDERBUSS_BULLET_SPEED)
            .build());

        blunderbussCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.pellet_count"), Config.blunderbussPelletCount)
            .setSaveConsumer(value -> Config.blunderbussPelletCount = value)
            .setMin(1)
            .setDefaultValue(Config.BLUNDERBUSS_PELLET_COUNT)
            .build());

        blunderbussCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.pellet_damage_multiplier"), Config.pelletDamageMultiplier)
            .setTooltip(Component.translatable("musketmod.options.pellet_damage_multiplier.tooltip"))
            .setSaveConsumer(value -> Config.pelletDamageMultiplier = value)
            .setMin(1.0f)
            .setDefaultValue(Config.PELLET_DAMAGE_MULTIPLIER)
            .build());

        blunderbussCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage_min"), Config.blunderbussDamageMin)
            .setSaveConsumer(value -> Config.blunderbussDamageMin = value)
            .setMin(0.5f)
            .setDefaultValue(Config.BLUNDERBUSS_DAMAGE_MIN)
            .build());

        blunderbussCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage_max"), Config.blunderbussDamageMax)
            .setSaveConsumer(value -> Config.blunderbussDamageMax = value)
            .setMin(0.5f)
            .setDefaultValue(Config.BLUNDERBUSS_DAMAGE_MAX)
            .build());

        blunderbussCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.durability"), Config.blunderbussDurability)
            .setTooltip(Component.translatable("musketmod.options.tooltip.restart"))
            .setSaveConsumer(value -> Config.blunderbussDurability = value)
            .setMin(1)
            .setDefaultValue(Config.BLUNDERBUSS_DURABILITY)
            .build());

        ConfigCategory pistolCategory = builder.getOrCreateCategory(Component.translatable("item.musketmod.pistol"));

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_std_dev"), Config.pistolBulletStdDev)
            .setTooltip(Component.translatable("musketmod.options.tooltip.degrees"))
            .setSaveConsumer(value -> Config.pistolBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.PISTOL_BULLET_STD_DEV)
            .build());

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_speed"), Config.pistolBulletSpeed)
            .setTooltip(Component.translatable("musketmod.options.tooltip.blocks_per_second"))
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

        pistolCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.reduction_per_quick_charge_level"), Config.reductionPerQuickChargeLevel)
            .setTooltip(Component.translatable("musketmod.options.tooltip.seconds"))
            .setSaveConsumer(value -> Config.reductionPerQuickChargeLevel = value)
            .setMin(0.1f)
            .setDefaultValue(Config.REDUCTION_PER_QUICK_CHARGE_LEVEL)
            .build());

        pistolCategory.addEntry(entryBuilder.startIntField(
            Component.translatable("musketmod.options.durability"), Config.pistolDurability)
            .setTooltip(Component.translatable("musketmod.options.tooltip.restart"))
            .setSaveConsumer(value -> Config.pistolDurability = value)
            .setMin(1)
            .setDefaultValue(Config.PISTOL_DURABILITY)
            .build());

        ConfigCategory dispenserCategory = builder.getOrCreateCategory(Component.translatable("block.minecraft.dispenser"));

        dispenserCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_std_dev"), Config.dispenserBulletStdDev)
            .setTooltip(Component.translatable("musketmod.options.tooltip.degrees"))
            .setSaveConsumer(value -> Config.dispenserBulletStdDev = value)
            .setMin(0.0f)
            .setDefaultValue(Config.DISPENSER_BULLET_STD_DEV)
            .build());

        dispenserCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.bullet_speed"), Config.dispenserBulletSpeed)
            .setTooltip(Component.translatable("musketmod.options.tooltip.blocks_per_second"))
            .setSaveConsumer(value -> Config.dispenserBulletSpeed = value)
            .setMin(1.0f)
            .setDefaultValue(Config.DISPENSER_BULLET_SPEED)
            .build());

        dispenserCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage_min"), Config.dispenserDamageMin)
            .setSaveConsumer(value -> Config.dispenserDamageMin = value)
            .setMin(0.5f)
            .setDefaultValue(Config.DISPENSER_DAMAGE_MIN)
            .build());

        dispenserCategory.addEntry(entryBuilder.startFloatField(
            Component.translatable("musketmod.options.damage_max"), Config.dispenserDamageMax)
            .setSaveConsumer(value -> Config.dispenserDamageMax = value)
            .setMin(0.5f)
            .setDefaultValue(Config.DISPENSER_DAMAGE_MAX)
            .build());

        builder.setSavingRunnable(() -> {
            Config.save();
        });

        return builder.build();
    }
}
