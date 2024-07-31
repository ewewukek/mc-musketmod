package ewewukek.musketmod;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class MusketItem extends GunItem {
    public static final float BAYONET_SPEED = -2.4f;

    public MusketItem(Item.Properties properties) {
        super(properties.durability(Config.musketDurability));
    }

    public static ItemAttributeModifiers createBayonetAttributes() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_ID, Config.bayonetDamage, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(
                BASE_ATTACK_SPEED_ID, BAYONET_SPEED, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }

    @Override
    public float bulletStdDev() {
        return (float)Config.musketBulletStdDev;
    }

    @Override
    public float bulletSpeed() {
        return (float)Config.musketBulletSpeed;
    }

    @Override
    public float damageMin() {
        return (float)Config.musketDamageMin;
    }

    @Override
    public float damageMax() {
        return (float)Config.musketDamageMax;
    }

    @Override
    public SoundEvent fireSound() {
        return Sounds.MUSKET_FIRE;
    }

    @Override
    public boolean twoHanded() {
        return true;
    }

    @Override
    public boolean ignoreInvulnerableTime() {
        return false;
    }
}
