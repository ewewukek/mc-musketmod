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

    public static float bulletMaxDistance;
    public static final float BULLET_MAX_DISTANCE = 256.0f;

    public static float pvpDamageMultiplier;
    public static final float PVP_DAMAGE_MULTIPLIER = 1.0f;

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
    public static final int BAYONET_DAMAGE = 4;

    public static float pistolBulletStdDev;
    public static final float PISTOL_BULLET_STD_DEV = 1.5f;
    public static float pistolBulletSpeed;
    public static final float PISTOL_BULLET_SPEED = 140.0f;
    public static float pistolDamageMin;
    public static final float PISTOL_DAMAGE_MIN = 12.0f;
    public static float pistolDamageMax;
    public static final float PISTOL_DAMAGE_MAX = 12.5f;
    public static int pistolDurability;
    public static final int PISTOL_DURABILITY = 150;

    public static void reload() {
        setDefaults();
        load();
        logger.info("Configuration has been loaded");
    }

    public static void setDefaults() {
        bulletMaxDistance = BULLET_MAX_DISTANCE;
        pvpDamageMultiplier = PVP_DAMAGE_MULTIPLIER;

        musketBulletStdDev = MUSKET_BULLET_STD_DEV;
        musketBulletSpeed = MUSKET_BULLET_SPEED;
        musketDamageMin = MUSKET_DAMAGE_MIN;
        musketDamageMax = MUSKET_DAMAGE_MAX;
        musketDurability = MUSKET_DURABILITY;
        bayonetDamage = BAYONET_DAMAGE;

        pistolBulletStdDev = PISTOL_BULLET_STD_DEV;
        pistolBulletSpeed = PISTOL_BULLET_SPEED;
        pistolDamageMin = PISTOL_DAMAGE_MIN;
        pistolDamageMax = PISTOL_DAMAGE_MAX;
        pistolDurability = PISTOL_DURABILITY;
    }

    public static void load() {
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
                    case "bulletMaxDistance":
                        bulletMaxDistance = value;
                        break;
                    case "pvpDamageMultiplier":
                        pvpDamageMultiplier = value;
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
            if (version < 2) {
                if (musketDamageMax == 21.5) musketDamageMax = 21;
            }
            save();
        }
    }

    public static void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(MusketMod.CONFIG_PATH)) {
            writer.write("version = "+VERSION+"\n");
            writer.write("\n");
            writer.write("# Maximum bullet travel distance (in blocks)\n");
            writer.write("bulletMaxDistance = "+bulletMaxDistance+"\n");
            writer.write("# Damage multiplier for pvp\n");
            writer.write("pvpDamageMultiplier = "+pvpDamageMultiplier+"\n");
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
            writer.write("# Added bayonet damage (applied on restart)\n");
            writer.write("bayonetDamage = "+bayonetDamage+"\n");
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

        } catch (IOException e) {
            logger.warn("Could not save configuration file: ", e);
        }
    }
}
