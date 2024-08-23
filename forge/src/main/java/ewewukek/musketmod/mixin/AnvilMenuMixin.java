package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import ewewukek.musketmod.VanillaHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {
    private Holder<Enchantment> testedEnchantment;

    @Redirect(method = "createResult", at = @At(value = "INVOKE",
        target = "Lit/unimi/dsi/fastutil/objects/Object2IntMap$Entry;getKey()Ljava/lang/Object;"))
    private Object storeEnchantment(Entry<Holder<Enchantment>> entry) {
        return (testedEnchantment = entry.getKey());
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean canEnchant(Enchantment enchantment, ItemStack stack) {
        if (enchantment == testedEnchantment.value()
        && VanillaHelper.canEnchant(testedEnchantment, stack)) {
            return true;
        }
        return enchantment.canEnchant(stack);
    }
}
