package ewewukek.musketmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Sounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MusketMod.MODID);
    public static final RegistryObject<SoundEvent> MUSKET_LOAD_0 = SOUND_EVENTS.register("musket_load0",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(MusketMod.MODID, "musket_load0"),
                    16f));
    public static final RegistryObject<SoundEvent> MUSKET_LOAD_1 = SOUND_EVENTS.register("musket_load1",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(MusketMod.MODID, "musket_load1"),
                    16f));
    public static final RegistryObject<SoundEvent> MUSKET_LOAD_2 =SOUND_EVENTS.register("musket_load2",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(MusketMod.MODID, "musket_load2"),
                    16f));
    public static final RegistryObject<SoundEvent> MUSKET_READY = SOUND_EVENTS.register("musket_ready",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(MusketMod.MODID, "musket_ready"),
                    16f));
    public static final RegistryObject<SoundEvent> MUSKET_FIRE = SOUND_EVENTS.register("musket_fire",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(MusketMod.MODID, "musket_fire"),
                    16f));
    public static final RegistryObject<SoundEvent> PISTOL_FIRE = SOUND_EVENTS.register("pistol_fire",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(MusketMod.MODID, "pistol_fire"),
                    16f));
}
