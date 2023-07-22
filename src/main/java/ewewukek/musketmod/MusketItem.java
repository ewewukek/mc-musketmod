package ewewukek.musketmod;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

public class MusketItem extends GunItem {
    public static final int DURABILITY = 250;
    public static final int BAYONET_DAMAGE = 4;
    public static final float BAYONET_SPEED = -2.4f;

    public static float bulletStdDev;
    public static float bulletSpeed;
    public static float damageMultiplierMin;
    public static float damageMultiplierMax;

    public final Multimap<Attribute, AttributeModifier> bayonetAttributeModifiers;

    public MusketItem(Item.Properties properties, boolean withBayonet) {
        super(properties.defaultDurability(DURABILITY));
        if (withBayonet) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", BAYONET_DAMAGE, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                BASE_ATTACK_SPEED_UUID, "Weapon modifier", BAYONET_SPEED, AttributeModifier.Operation.ADDITION));
            bayonetAttributeModifiers = builder.build();
        } else {
            bayonetAttributeModifiers = null;
        }
    }

    @Override
    public float bulletStdDev() {
        return bulletStdDev;
    }

    @Override
    public float bulletSpeed() {
        return bulletSpeed;
    }

    @Override
    public float damageMultiplierMin() {
        return damageMultiplierMin;
    }

    @Override
    public float damageMultiplierMax() {
        return damageMultiplierMax;
    }

    @Override
    public SoundEvent fireSound() {
        return Sounds.MUSKET_FIRE.get();
    }

    @Override
    public boolean twoHanded() {
        return true;
    }

    @Override
    public boolean ignoreInvulnerableTime() {
        return false;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND && bayonetAttributeModifiers != null
                ? bayonetAttributeModifiers : super.getDefaultAttributeModifiers(slot);
    }
}
