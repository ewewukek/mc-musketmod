package ewewukek.musketmod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Locale;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
    private static final Logger logger = LogManager.getLogger(MusketMod.class);
    public static final int VERSION = 4;

    public static int loadingStages;
    public static final int LOADING_STAGES = 4;
    public static float loadingStageDuration;
    public static final float LOADING_STAGE_DURATION = 0.45f;
    public static float bulletMaxDistance;
    public static final float BULLET_MAX_DISTANCE = 256.0f;
    public static float pvpDamageMultiplier;
    public static final float PVP_DAMAGE_MULTIPLIER = 1.0f;
    public static float mobDamageMultiplier;
    public static final float MOB_DAMAGE_MULTIPLIER = 0.5f;
    public static float pistolPillagerChance;
    public static final float PISTOL_PILLAGER_CHANCE = 0.2f;
    public static float musketSkeletonChance;
    public static final float MUSKET_SKELETON_CHANCE = 0.05f;

    public static float musketBulletStdDev;
    public static final float MUSKET_BULLET_STD_DEV = 1.0f;
    public static float musketBulletSpeed;
    public static final float MUSKET_BULLET_SPEED = 180.0f;
    public static float musketDamageMin;
    public static final float MUSKET_DAMAGE_MIN = 20.5f;
    public static float musketDamageMax;
    public static final float MUSKET_DAMAGE_MAX = 21.0f;
    public static int musketDurability;
    public static final int MUSKET_DURABILITY = 250;

    public static int bayonetDamage;
    public static final int BAYONET_DAMAGE = 5;

    public static float scopedMusketBulletStdDev;
    public static final float SCOPED_MUSKET_BULLET_STD_DEV = 0.2f;
    public static float scopeZoom;
    public static final float SCOPE_ZOOM = 4.0f;
    public static float damagePerPowerLevel;
    public static final float DAMAGE_PER_POWER_LEVEL = 0.5f;
    public static float dropReductionPerPowerLevel;
    public static final float DROP_REDUCTION_PER_POWER_LEVEL = 0.1f;
    public static int scopedMusketDurability;
    public static final int SCOPED_MUSKET_DURABILITY = 150;

    public static float blunderbussBulletStdDev;
    public static final float BLUNDERBUSS_BULLET_STD_DEV = 1.5f;
    public static float blunderbussBulletSpeed;
    public static final float BLUNDERBUSS_BULLET_SPEED = 160.0f;
    public static int blunderbussPelletCount;
    public static final int BLUNDERBUSS_PELLET_COUNT = 9;
    public static float pelletDamageMultiplier;
    public static final float PELLET_DAMAGE_MULTIPLIER = 1.5f;
    public static float blunderbussDamageMin;
    public static final float BLUNDERBUSS_DAMAGE_MIN = 20.5f;
    public static float blunderbussDamageMax;
    public static final float BLUNDERBUSS_DAMAGE_MAX = 21.0f;
    public static int blunderbussDurability;
    public static final int BLUNDERBUSS_DURABILITY = 200;

    public static float pistolBulletStdDev;
    public static final float PISTOL_BULLET_STD_DEV = 1.5f;
    public static float pistolBulletSpeed;
    public static final float PISTOL_BULLET_SPEED = 140.0f;
    public static float pistolDamageMin;
    public static final float PISTOL_DAMAGE_MIN = 12.0f;
    public static float pistolDamageMax;
    public static final float PISTOL_DAMAGE_MAX = 12.5f;
    public static int pistolDurability;
    public static final int PISTOL_DURABILITY = 200;

    public static float dispenserBulletStdDev;
    public static final float DISPENSER_BULLET_STD_DEV = 2.0f;
    public static float dispenserBulletSpeed;
    public static final float DISPENSER_BULLET_SPEED = 120.0f;
    public static float dispenserDamageMin;
    public static final float DISPENSER_DAMAGE_MIN = 10.0f;
    public static float dispenserDamageMax;
    public static final float DISPENSER_DAMAGE_MAX = 10.5f;

    public static void load() {
        loadingStages = LOADING_STAGES;
        loadingStageDuration = LOADING_STAGE_DURATION;
        bulletMaxDistance = BULLET_MAX_DISTANCE;
        pvpDamageMultiplier = PVP_DAMAGE_MULTIPLIER;
        mobDamageMultiplier = MOB_DAMAGE_MULTIPLIER;
        pistolPillagerChance = PISTOL_PILLAGER_CHANCE;
        musketSkeletonChance = MUSKET_SKELETON_CHANCE;

        musketBulletStdDev = MUSKET_BULLET_STD_DEV;
        musketBulletSpeed = MUSKET_BULLET_SPEED;
        musketDamageMin = MUSKET_DAMAGE_MIN;
        musketDamageMax = MUSKET_DAMAGE_MAX;
        musketDurability = MUSKET_DURABILITY;

        bayonetDamage = BAYONET_DAMAGE;

        scopedMusketBulletStdDev = SCOPED_MUSKET_BULLET_STD_DEV;
        scopeZoom = SCOPE_ZOOM;
        damagePerPowerLevel = DAMAGE_PER_POWER_LEVEL;
        dropReductionPerPowerLevel = DROP_REDUCTION_PER_POWER_LEVEL;
        scopedMusketDurability = SCOPED_MUSKET_DURABILITY;

        blunderbussBulletStdDev = BLUNDERBUSS_BULLET_STD_DEV;
        blunderbussBulletSpeed = BLUNDERBUSS_BULLET_SPEED;
        blunderbussPelletCount = BLUNDERBUSS_PELLET_COUNT;
        pelletDamageMultiplier = PELLET_DAMAGE_MULTIPLIER;
        blunderbussDamageMin = BLUNDERBUSS_DAMAGE_MIN;
        blunderbussDamageMax = BLUNDERBUSS_DAMAGE_MAX;
        blunderbussDurability = BLUNDERBUSS_DURABILITY;

        pistolBulletStdDev = PISTOL_BULLET_STD_DEV;
        pistolBulletSpeed = PISTOL_BULLET_SPEED;
        pistolDamageMin = PISTOL_DAMAGE_MIN;
        pistolDamageMax = PISTOL_DAMAGE_MAX;
        pistolDurability = PISTOL_DURABILITY;

        dispenserBulletStdDev = DISPENSER_BULLET_STD_DEV;
        dispenserBulletSpeed = DISPENSER_BULLET_SPEED;
        dispenserDamageMin = DISPENSER_DAMAGE_MIN;
        dispenserDamageMax = DISPENSER_DAMAGE_MAX;

        int version = 0;
        try (BufferedReader reader = Files.newBufferedReader(MusketMod.CONFIG_PATH)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                int commentStart = line.indexOf('#');
                if (commentStart != -1) line = line.substring(0, commentStart);

                line.trim();
                if (line.length() == 0) continue;

                String errorPrefix = MusketMod.CONFIG_PATH+": line "+lineNumber+": ";
                try (Scanner s = new Scanner(line)) {
                    s.useLocale(Locale.US);
                    s.useDelimiter("\\s*=\\s*");

                    if (!s.hasNext()) {
                        logger.warn(errorPrefix+"parameter name is missing");
                        continue;
                    }
                    String key = s.next().trim();

                    if (!s.hasNextFloat()) {
                        logger.warn(errorPrefix+"value is missing/wrong/not a number");
                        continue;
                    }
                    float value = s.nextFloat();

                    switch (key) {
                    case "version":
                        version = (int)value;
                        break;

                    case "loadingStages":
                        loadingStages = (int)value;
                        break;
                    case "loadingStageDuration":
                        loadingStageDuration = value;
                        break;
                    case "bulletMaxDistance":
                        bulletMaxDistance = value;
                        break;
                    case "pvpDamageMultiplier":
                        pvpDamageMultiplier = value;
                        break;
                    case "mobDamageMultiplier":
                        mobDamageMultiplier = value;
                        break;
                    case "pistolPillagerChance":
                        pistolPillagerChance = value;
                        break;
                    case "musketSkeletonChance":
                        musketSkeletonChance = value;
                        break;

                    case "bulletStdDev": // COMPAT
                    case "musketBulletStdDev":
                        musketBulletStdDev = value;
                        break;
                    case "bulletSpeed": // COMPAT
                    case "musketBulletSpeed":
                        musketBulletSpeed = value;
                        break;
                    case "damageMin": // COMPAT
                    case "musketDamageMin":
                        musketDamageMin = value;
                        break;
                    case "damageMax": // COMPAT
                    case "musketDamageMax":
                        musketDamageMax = value;
                        break;
                    case "durability": // COMPAT
                    case "musketDurability":
                        musketDurability = (int)value;
                        break;

                    case "bayonetDamage":
                        bayonetDamage = (int)value;
                        break;

                    case "scopedMusketBulletStdDev":
                        scopedMusketBulletStdDev = value;
                        break;
                    case "scopeZoom":
                        scopeZoom = value;
                        break;
                    case "damagePerPowerLevel":
                        damagePerPowerLevel = value;
                        break;
                    case "dropReductionPerPowerLevel":
                        dropReductionPerPowerLevel = value;
                        break;
                    case "scopedMusketDurability":
                        scopedMusketDurability = (int)value;
                        break;

                    case "blunderbussBulletStdDev":
                        blunderbussBulletStdDev = value;
                        break;
                    case "blunderbussBulletSpeed":
                        blunderbussBulletSpeed = value;
                        break;
                    case "blunderbussPelletCount":
                        blunderbussPelletCount = (int)value;
                        break;
                    case "pelletDamageMultiplier":
                        pelletDamageMultiplier = value;
                        break;
                    case "blunderbussDamageMin":
                        blunderbussDamageMin = value;
                        break;
                    case "blunderbussDamageMax":
                        blunderbussDamageMax = value;
                        break;
                    case "blunderbussDurability":
                        blunderbussDurability = (int)value;
                        break;

                    case "pistolBulletStdDev":
                        pistolBulletStdDev = value;
                        break;
                    case "pistolBulletSpeed":
                        pistolBulletSpeed = value;
                        break;
                    case "pistolDamageMin":
                        pistolDamageMin = value;
                        break;
                    case "pistolDamageMax":
                        pistolDamageMax = value;
                        break;
                    case "pistolDurability":
                        pistolDurability = (int)value;
                        break;

                    case "dispenserBulletStdDev":
                        dispenserBulletStdDev = value;
                        break;
                    case "dispenserBulletSpeed":
                        dispenserBulletSpeed = value;
                        break;
                    case "dispenserDamageMin":
                        dispenserDamageMin = value;
                        break;
                    case "dispenserDamageMax":
                        dispenserDamageMax = value;
                        break;
                    default:
                        logger.warn(errorPrefix+"unrecognized parameter name: "+key);
                    }
                }
            }
        } catch (NoSuchFileException e) {
            save();
            logger.info("Configuration file not found, default created");

        } catch (IOException e) {
            logger.warn("Could not read configuration file: ", e);
        }
        if (version < VERSION) {
            logger.info("Configuration file belongs to older version, updating");
            if (version < 4) {
                if (bayonetDamage == 4) bayonetDamage = 5;
                if (pistolDurability == 150) pistolDurability = 200;
            }
            if (version < 2) {
                if (musketDamageMax == 21.5) musketDamageMax = 21;
            }
            save();
        }
        logger.info("Configuration has been loaded");
    }

    public static void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(MusketMod.CONFIG_PATH)) {
            writer.write("version = "+VERSION+"\n");
            writer.write("\n");

            writer.write("# Number of loading stages\n");
            writer.write("loadingStages = "+loadingStages+"\n");
            writer.write("# Loading stage duration (in seconds)\n");
            writer.write("loadingStageDuration = "+loadingStageDuration+"\n");
            writer.write("# Maximum bullet travel distance (in blocks)\n");
            writer.write("bulletMaxDistance = "+bulletMaxDistance+"\n");
            writer.write("# Damage multiplier for pvp\n");
            writer.write("pvpDamageMultiplier = "+pvpDamageMultiplier+"\n");
            writer.write("# Damage multiplier for monsters\n");
            writer.write("mobDamageMultiplier = "+mobDamageMultiplier+"\n");
            writer.write("# Probability of Pillager having a pistol\n");
            writer.write("pistolPillagerChance = "+pistolPillagerChance+"\n");
            writer.write("# Probability of Skeleton having a musket\n");
            writer.write("musketSkeletonChance = "+musketSkeletonChance+"\n");
            writer.write("\n");

            writer.write("# Musket\n");
            writer.write("\n");
            writer.write("# Standard deviation of bullet spread (in degrees)\n");
            writer.write("musketBulletStdDev = "+musketBulletStdDev+"\n");
            writer.write("# Muzzle velocity of bullet (in blocks per second)\n");
            writer.write("musketBulletSpeed = "+musketBulletSpeed+"\n");
            writer.write("# Minimum damage at point-blank range\n");
            writer.write("musketDamageMin = "+musketDamageMin+"\n");
            writer.write("# Maximum damage at point-blank range\n");
            writer.write("musketDamageMax = "+musketDamageMax+"\n");
            writer.write("# Durability (applied on restart)\n");
            writer.write("musketDurability = "+musketDurability+"\n");
            writer.write("\n");

            writer.write("# Musket with bayonet\n");
            writer.write("\n");
            writer.write("# Bayonet damage (applied on restart)\n");
            writer.write("bayonetDamage = "+bayonetDamage+"\n");
            writer.write("\n");

            writer.write("# Musket with scope\n");
            writer.write("\n");
            writer.write("# Standard deviation of bullet spread (in degrees)\n");
            writer.write("scopedMusketBulletStdDev = "+scopedMusketBulletStdDev+"\n");
            writer.write("# Scope zoom factor\n");
            writer.write("scopeZoom = "+scopeZoom+"\n");
            writer.write("# Extra damage per Power enchantment level\n");
            writer.write("damagePerPowerLevel = "+damagePerPowerLevel+"\n");
            writer.write("# Bullet drop reduction per Power enchantment level\n");
            writer.write("dropReductionPerPowerLevel = "+dropReductionPerPowerLevel+"\n");
            writer.write("# Durability (applied on restart)\n");
            writer.write("scopedMusketDurability = "+scopedMusketDurability+"\n");
            writer.write("\n");

            writer.write("# Blunderbuss\n");
            writer.write("\n");
            writer.write("# Standard deviation of bullet spread (in degrees)\n");
            writer.write("blunderbussBulletStdDev = "+blunderbussBulletStdDev+"\n");
            writer.write("# Muzzle velocity of bullet (in blocks per second)\n");
            writer.write("blunderbussBulletSpeed = "+blunderbussBulletSpeed+"\n");
            writer.write("# Pellet count\n");
            writer.write("blunderbussPelletCount = "+blunderbussPelletCount+"\n");
            writer.write("# Individual pellet damage multiplier\n");
            writer.write("pelletDamageMultiplier = "+pelletDamageMultiplier+"\n");
            writer.write("# Minimum damage at point-blank range\n");
            writer.write("blunderbussDamageMin = "+blunderbussDamageMin+"\n");
            writer.write("# Maximum damage at point-blank range\n");
            writer.write("blunderbussDamageMax = "+blunderbussDamageMax+"\n");
            writer.write("# Durability (applied on restart)\n");
            writer.write("blunderbussDurability = "+blunderbussDurability+"\n");
            writer.write("\n");

            writer.write("# Pistol\n");
            writer.write("\n");
            writer.write("# Standard deviation of bullet spread (in degrees)\n");
            writer.write("pistolBulletStdDev = "+pistolBulletStdDev+"\n");
            writer.write("# Muzzle velocity of bullet (in blocks per second)\n");
            writer.write("pistolBulletSpeed = "+pistolBulletSpeed+"\n");
            writer.write("# Minimum damage at point-blank range\n");
            writer.write("pistolDamageMin = "+pistolDamageMin+"\n");
            writer.write("# Maximum damage at point-blank range\n");
            writer.write("pistolDamageMax = "+pistolDamageMax+"\n");
            writer.write("# Durability (applied on restart)\n");
            writer.write("pistolDurability = "+pistolDurability+"\n");
            writer.write("\n");

            writer.write("# Dispenser\n");
            writer.write("\n");
            writer.write("# Standard deviation of bullet spread (in degrees)\n");
            writer.write("dispenserBulletStdDev = "+dispenserBulletStdDev+"\n");
            writer.write("# Muzzle velocity of bullet (in blocks per second)\n");
            writer.write("dispenserBulletSpeed = "+dispenserBulletSpeed+"\n");
            writer.write("# Minimum damage at point-blank range\n");
            writer.write("dispenserDamageMin = "+dispenserDamageMin+"\n");
            writer.write("# Maximum damage at point-blank range\n");
            writer.write("dispenserDamageMax = "+dispenserDamageMax+"\n");

        } catch (IOException e) {
            logger.warn("Could not save configuration file: ", e);
        }
    }
}
