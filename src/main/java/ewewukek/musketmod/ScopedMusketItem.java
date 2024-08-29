package ewewukek.musketmod;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ScopedMusketItem extends MusketItem {
    public static final int RECOIL_TICKS = 12;
    public static final float RECOIL_AMOUNT = 0.25f;

    // for client-side logic
    public static boolean isScoping;
    public static int recoilTicks;

    public ScopedMusketItem(Properties properties) {
        super(properties, false);
    }

    @Override
    public float bulletStdDev() {
        return Config.scopedMusketBulletStdDev;
    }

    @Override
    public float bulletDropReduction() {
        return 1.0f - Config.bulletGravityMultiplier;
    }

    @Override
    public int hitDurabilityDamage() {
        return 2;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        boolean wasLoaded = GunItem.isLoaded(player.getItemInHand(hand));
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);

        if (level.isClientSide && wasLoaded && result.getResult().consumesAction()) {
            ScopedMusketItem.recoilTicks = ScopedMusketItem.RECOIL_TICKS;
        }
        return result;
    }
}
