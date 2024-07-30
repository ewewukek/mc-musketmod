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
    public static final Config INSTANCE = new Config();
    public static final int VERSION = 3;

    public double bulletMaxDistance;

    public double musketBulletStdDev;
    public double musketBulletSpeed;
    public double musketDamageMin;
    public double musketDamageMax;
    public int musketDurability;
    public int bayonetDamage;

    public double pistolBulletStdDev;
    public double pistolBulletSpeed;
    public double pistolDamageMin;
    public double pistolDamageMax;
    public int pistolDurability;

    public static void reload() {
        INSTANCE.setDefaults();
        INSTANCE.load();

        BulletEntity.maxDistance = INSTANCE.bulletMaxDistance;

        MusketItem.bulletStdDev = (float)Math.toRadians(INSTANCE.musketBulletStdDev);
        MusketItem.bulletSpeed = (float)(INSTANCE.musketBulletSpeed / 20);
        double maxEnergy = MusketItem.bulletSpeed * MusketItem.bulletSpeed;
        MusketItem.damageMultiplierMin = (float)(INSTANCE.musketDamageMin / maxEnergy);
        MusketItem.damageMultiplierMax = (float)(INSTANCE.musketDamageMax / maxEnergy);
        MusketItem.durability = INSTANCE.musketDurability;
        MusketItem.bayonetDamage = INSTANCE.bayonetDamage;

        PistolItem.bulletStdDev = (float)Math.toRadians(INSTANCE.pistolBulletStdDev);
        PistolItem.bulletSpeed = (float)(INSTANCE.pistolBulletSpeed / 20);
        maxEnergy = PistolItem.bulletSpeed * PistolItem.bulletSpeed;
        PistolItem.damageMultiplierMin = (float)(INSTANCE.pistolDamageMin / maxEnergy);
        PistolItem.damageMultiplierMax = (float)(INSTANCE.pistolDamageMax / maxEnergy);
        PistolItem.durability = INSTANCE.pistolDurability;

        logger.info("Configuration has been loaded");
    }

    private void setDefaults() {
        bulletMaxDistance = 256;

        musketBulletStdDev = 1;
        musketBulletSpeed = 180;
        musketDamageMin = 20.5;
        musketDamageMax = 21;
        musketDurability = 250;
        bayonetDamage = 4;

        pistolBulletStdDev = 1.5;
        pistolBulletSpeed = 140;
        pistolDamageMin = 12;
        pistolDamageMax = 12.5;
        pistolDurability = 150;
    }

    private void load() {
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

                    if (!s.hasNextDouble()) {
                        logger.warn(errorPrefix+"value is missing/wrong/not a number");
                        continue;
                    }
                    double value = s.nextDouble();

                    switch (key) {
                    case "version":
                        version = (int)value;
                        break;
                    case "bulletMaxDistance":
                        bulletMaxDistance = value;
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

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(MusketMod.CONFIG_PATH)) {
            writer.write("version = "+VERSION+"\n");
            writer.write("\n");
            writer.write("# Maximum bullet travel distance (in blocks)\n");
            writer.write("bulletMaxDistance = "+bulletMaxDistance+"\n");
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
