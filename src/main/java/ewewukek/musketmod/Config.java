package ewewukek.musketmod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.loading.FMLPaths;

public class Config {
    private static final Logger logger = LogManager.getLogger(MusketMod.class);

    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("musketmod.txt");
    public static final Config INSTANCE = new Config();

    public static final int VERSION = 1;

    public double bulletStdDev;
    public double bulletSpeed;
    public double bulletMaxDistance;
    public double damageMin;
    public double damageMax;

    public static void reload() {
        INSTANCE.setDefaults();
        INSTANCE.load();

        MusketItem.bulletStdDev = (float)Math.toRadians(INSTANCE.bulletStdDev);
        MusketItem.bulletSpeed = INSTANCE.bulletSpeed / 20;
        double maxEnergy = MusketItem.bulletSpeed * MusketItem.bulletSpeed;
        BulletEntity.damageFactorMin = (float)(INSTANCE.damageMin / maxEnergy);
        BulletEntity.damageFactorMax = (float)(INSTANCE.damageMax / maxEnergy);
        BulletEntity.maxDistance = INSTANCE.bulletMaxDistance;

        logger.info("Configuration has been loaded");
    }

    private void setDefaults() {
        bulletStdDev = 1;
        bulletSpeed = 180;
        bulletMaxDistance = 256;
        damageMin = 20.5;
        damageMax = 21.5;
    }

    private void load() {
        int version = 0;
        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                int commentStart = line.indexOf('#');
                if (commentStart != -1) line = line.substring(0, commentStart);

                line.trim();
                if (line.length() == 0) continue;

                String errorPrefix = CONFIG_PATH+": line "+lineNumber+": ";
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
                    case "bulletStdDev":
                        bulletStdDev = value;
                        break;
                    case "bulletSpeed":
                        bulletSpeed = value;
                        break;
                    case "bulletMaxDistance":
                        bulletMaxDistance = value;
                    case "damageMin":
                        damageMin = value;
                        break;
                    case "damageMax":
                        damageMax = value;
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
            save();
        }
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            writer.write("version = "+VERSION+"\n");
            writer.write("# Standard deviation of bullet spread (in degrees)\n");
            writer.write("bulletStdDev = "+bulletStdDev+"\n");
            writer.write("# Muzzle velocity of bullet (in blocks per second)\n");
            writer.write("bulletSpeed = "+bulletSpeed+"\n");
            writer.write("# Maximum bullet travel distance (in blocks)\n");
            writer.write("bulletMaxDistance = "+bulletMaxDistance+"\n");
            writer.write("# Minimum damage at point-blank range\n");
            writer.write("damageMin = "+damageMin+"\n");
            writer.write("# Maximum damage at point-blank range\n");
            writer.write("damageMax = "+damageMax+"\n");

        } catch (IOException e) {
            logger.warn("Could not save configuration file: ", e);
        }
    }
}
