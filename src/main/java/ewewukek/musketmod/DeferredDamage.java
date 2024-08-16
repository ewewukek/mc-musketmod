package ewewukek.musketmod;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class DeferredDamage {
    public static void hurt(Entity target, DamageSource source, float damage) {
        Entry entry = entries.get(target);
        if (entry == null) {
            entry = new Entry();
            entries.put(target, entry);
        }
        entry.source = source;
        entry.damage += damage;
    }

    public static void igniteForSeconds(Entity target, float seconds) {
        Entry entry = entries.get(target);
        if (entry == null) {
            entry = new Entry();
            entries.put(target, entry);
        }
        entry.igniteSeconds = Math.max(entry.igniteSeconds, seconds);
    }

    public static void apply() {
        entries.forEach((target, entry) -> {
            if (entry.damage > 0) target.hurt(entry.source, entry.damage);
            if (entry.igniteSeconds > 0) target.igniteForSeconds(entry.igniteSeconds);
        });
        entries.clear();
    }

    private static Map<Entity, Entry> entries = new HashMap<>();

    private static class Entry {
        DamageSource source;
        float damage;
        float igniteSeconds;
    }
}
