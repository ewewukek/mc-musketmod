package ewewukek.musketmod;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

public class MusketUpgradeItem extends SmithingTemplateItem {
    private static final ChatFormatting TITLE = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION = ChatFormatting.BLUE;
    private static final ResourceLocation EMPTY_SLOT_MUSKET = ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, "item/empty_slot_musket");
    private static final ResourceLocation EMPTY_SLOT_SWORD = ResourceLocation.withDefaultNamespace("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_SPYGLASS = ResourceLocation.withDefaultNamespace("item/empty_slot_spyglass");

    public MusketUpgradeItem() {
        super(
            modItemComponent("musket").withStyle(DESCRIPTION),
            modItemComponent("musket_upgrade.ingredients").withStyle(DESCRIPTION),
            modItemComponent("musket_upgrade").withStyle(TITLE),
            modItemComponent("musket_upgrade.base_slot_description"),
            modItemComponent("musket_upgrade.additions_slot_description"),
            List.of(EMPTY_SLOT_MUSKET),
            List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_SPYGLASS)
        );
    }

    private static MutableComponent modItemComponent(String key) {
        return Component.translatable(Util.makeDescriptionId("item",
            ResourceLocation.fromNamespaceAndPath(MusketMod.MODID, key)));
    }
}
