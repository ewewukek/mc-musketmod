package ewewukek.musketmod;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class MusketItem extends GunItem {
    public MusketItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createBayonetAttributes() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_ID, Config.bayonetDamage - 1, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(
                BASE_ATTACK_SPEED_ID, Config.bayonetSpeed - 4, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }

    @Override
    public float bulletStdDev() {
        return Config.musketBulletStdDev;
    }

    @Override
    public float bulletSpeed() {
        return Config.musketBulletSpeed;
    }

    @Override
    public float damage() {
        return Config.musketDamage;
    }

    @Override
    public SoundEvent fireSound(ItemStack stack) {
        return Sounds.MUSKET_FIRE;
    }
}
