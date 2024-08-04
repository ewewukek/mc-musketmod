package ewewukek.musketmod;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class Sounds {
    public static final SoundEvent MUSKET_LOAD_0 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "musket_load0"));
    public static final SoundEvent MUSKET_LOAD_1 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "musket_load1"));
    public static final SoundEvent MUSKET_LOAD_2 = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "musket_load2"));
    public static final SoundEvent MUSKET_READY = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "musket_ready"));
    public static final SoundEvent MUSKET_FIRE = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "musket_fire"));
    public static final SoundEvent BLUNDERBUSS_FIRE = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "blunderbuss_fire"));
    public static final SoundEvent PISTOL_FIRE = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "pistol_fire"));
    public static final SoundEvent DISPENSER_FIRE = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "dispenser_fire"));

    public static void register(Consumer<SoundEvent> helper) {
        helper.accept(MUSKET_LOAD_0);
        helper.accept(MUSKET_LOAD_1);
        helper.accept(MUSKET_LOAD_2);
        helper.accept(MUSKET_READY);
        helper.accept(MUSKET_FIRE);
        helper.accept(BLUNDERBUSS_FIRE);
        helper.accept(PISTOL_FIRE);
        helper.accept(DISPENSER_FIRE);
    }
}
