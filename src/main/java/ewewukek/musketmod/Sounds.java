package ewewukek.musketmod;

import java.util.function.Consumer;

import net.minecraft.sounds.SoundEvent;

public class Sounds {
    public static final SoundEvent MUSKET_LOAD_0 = SoundEvent.createVariableRangeEvent(MusketMod.resource("musket_load0"));
    public static final SoundEvent MUSKET_LOAD_1 = SoundEvent.createVariableRangeEvent(MusketMod.resource("musket_load1"));
    public static final SoundEvent MUSKET_LOAD_2 = SoundEvent.createVariableRangeEvent(MusketMod.resource("musket_load2"));
    public static final SoundEvent MUSKET_READY = SoundEvent.createVariableRangeEvent(MusketMod.resource("musket_ready"));
    public static final SoundEvent MUSKET_FIRE = SoundEvent.createVariableRangeEvent(MusketMod.resource("musket_fire"));
    public static final SoundEvent BLUNDERBUSS_FIRE = SoundEvent.createVariableRangeEvent(MusketMod.resource("blunderbuss_fire"));
    public static final SoundEvent BLUNDERBUSS_FIRE_FLAME = SoundEvent.createVariableRangeEvent(MusketMod.resource("blunderbuss_fire_flame"));
    public static final SoundEvent PISTOL_FIRE = SoundEvent.createVariableRangeEvent(MusketMod.resource("pistol_fire"));
    public static final SoundEvent DISPENSER_FIRE = SoundEvent.createVariableRangeEvent(MusketMod.resource("dispenser_fire"));
    public static final SoundEvent BULLET_FLY_BY = SoundEvent.createVariableRangeEvent(MusketMod.resource("bullet_fly_by"));
    public static final SoundEvent BULLET_WATER_HIT = SoundEvent.createVariableRangeEvent(MusketMod.resource("bullet_water_hit"));

    public static void register(Consumer<SoundEvent> helper) {
        helper.accept(MUSKET_LOAD_0);
        helper.accept(MUSKET_LOAD_1);
        helper.accept(MUSKET_LOAD_2);
        helper.accept(MUSKET_READY);
        helper.accept(MUSKET_FIRE);
        helper.accept(BLUNDERBUSS_FIRE);
        helper.accept(BLUNDERBUSS_FIRE_FLAME);
        helper.accept(PISTOL_FIRE);
        helper.accept(DISPENSER_FIRE);
        helper.accept(BULLET_FLY_BY);
        helper.accept(BULLET_WATER_HIT);
    }
}
