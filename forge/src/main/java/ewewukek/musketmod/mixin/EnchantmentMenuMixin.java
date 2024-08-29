package ewewukek.musketmod.mixin;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ewewukek.musketmod.IEnchantmentId;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

@Mixin(EnchantmentMenu.class)
abstract class EnchantmentMenuMixin {
    @Inject(method = "getEnchantmentList", at = @At("HEAD"))
    private void getEnchantmentList(RegistryAccess registryAccess, ItemStack stack, int seed, int enchantability, CallbackInfoReturnable<List<EnchantmentInstance>> ci) {
        Optional<HolderSet.Named<Enchantment>> enchantments = registryAccess
            .registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (enchantments.isPresent()) {
            enchantments.get().forEach(holder -> {
                ((IEnchantmentId)(Object)holder.value()).setHolder(holder);
            });
        }
    }
}
