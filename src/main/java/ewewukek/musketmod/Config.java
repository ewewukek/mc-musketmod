package ewewukek.musketmod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.loading.FMLPaths;

public class Config {
    private static final Logger logger = LogManager.getLogger(MusketMod.class);

    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("musketmod.txt");
    public static final Config INSTANCE = new Config();

    public double bulletStdDev;
    public double bulletSpeed;
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

        logger.info("Configuration has been loaded");
    }

    private void setDefaults() {
        bulletStdDev = 1;
        bulletSpeed = 180;
        damageMin = 20.5;
        damageMax = 21.5;
    }

    private boolean load() {
        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                int commentStart = line.indexOf('#');
                if (commentStart != -1) line = line.substring(0, commentStart);

                line.trim();
                if (line.length() == 0) continue;

                try (Scanner s = new Scanner(line).useDelimiter("\\s*=\\s*")) {

                    if (!s.hasNext()) throw new ReadException(lineNumber, "key is missing");
                    String key = s.next().trim();

                    if (!s.hasNextDouble()) throw new ReadException(lineNumber, "value is missing/wrong/not a number");
                    double value = s.nextDouble();

                    switch (key) {
                    case "bulletStdDev":
                        bulletStdDev = value;
                        break;
                    case "bulletSpeed":
                        bulletSpeed = value;
                        break;
                    case "damageMin":
                        damageMin = value;
                        break;
                    case "damageMax":
                        damageMax = value;
                        break;
                    default:
                        throw new ReadException(lineNumber, "unrecognized key: "+key);
                    }
                }
            }
        } catch (ReadException e) {
            logger.warn("Configuration file is corrupted: ", e);
            return false;

        } catch (NoSuchFileException e) {
            save();
            logger.info("Configuration file not found, default created");

        } catch (IOException e) {
            logger.warn("Could not read configuration file: ", e);
            return false;

        }
        return true;
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            writer.write("# Standard deviation of bullet spread (in degrees)\n");
            writer.write("bulletStdDev = "+bulletStdDev+"\n");
            writer.write("# Muzzle velocity of bullet (in blocks per second)\n");
            writer.write("bulletSpeed = "+bulletSpeed+"\n");
            writer.write("# Minimum damage at point-blank range\n");
            writer.write("damageMin = "+damageMin+"\n");
            writer.write("# Maximum damage at point-blank range\n");
            writer.write("damageMax = "+damageMax+"\n");

        } catch (IOException e) {
            logger.warn("Could not save configuration file: ", e);
        }
    }

    @SuppressWarnings("serial")
    public static class ReadException extends Exception {
        public ReadException(int lineNumber, String message) {
            super("line "+lineNumber+": "+message);
        }
    }
}
