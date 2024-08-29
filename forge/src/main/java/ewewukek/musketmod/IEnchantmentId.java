package ewewukek.musketmod;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

public interface IEnchantmentId {
    Holder<Enchantment> getHolder();
    void setHolder(Holder<Enchantment> holder);
}
