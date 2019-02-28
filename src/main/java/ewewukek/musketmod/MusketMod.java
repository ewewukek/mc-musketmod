package ewewukek.musketmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("musketmod")
public class MusketMod {
    public MusketMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
