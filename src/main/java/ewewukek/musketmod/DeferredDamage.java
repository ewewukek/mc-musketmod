package ewewukek.musketmod;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class DeferredDamage {
    public static void hurt(Entity target, DamageSource source, float damage, float igniteSeconds) {
        Entry entry = entries.get(target);
        if (entry == null) {
            entry = new Entry();
            entries.put(target, entry);
        }
        entry.source = source;
        entry.damage += damage;
        entry.igniteSeconds = Math.max(entry.igniteSeconds, igniteSeconds);
    }

    public static void apply() {
        entries.forEach((target, entry) -> {
            target.invulnerableTime = 0;
            target.hurt(entry.source, entry.damage);
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
