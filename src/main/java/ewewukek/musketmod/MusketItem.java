package ewewukek.musketmod;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

public class MusketItem extends GunItem {
    public static final int DURABILITY = 250;
    public static final int BAYONET_DAMAGE = 3;

    public static float bulletStdDev;
    public static float bulletSpeed;

    public final boolean hasBayonet;
    public final Multimap<Attribute, AttributeModifier> bayonetAttributeModifiers;

    public MusketItem(Item.Properties properties, boolean withBayonet) {
        super(properties.defaultDurability(DURABILITY));
        hasBayonet = withBayonet;

        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
            BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", BAYONET_DAMAGE, AttributeModifier.Operation.ADDITION));
        bayonetAttributeModifiers = builder.build();
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
    public SoundEvent fireSound() {
        return MusketMod.SOUND_MUSKET_FIRE;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND && hasBayonet
                ? bayonetAttributeModifiers
                : super.getDefaultAttributeModifiers(slot);
    }
}
