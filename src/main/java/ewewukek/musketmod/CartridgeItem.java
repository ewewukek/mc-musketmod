package ewewukek.musketmod;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class CartridgeItem extends Item {
    public CartridgeItem(Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }

    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Override
        public ItemStack execute(BlockSource blockSource, ItemStack stack) {
            ServerLevel level = blockSource.level();

            Direction blockDirection = blockSource.state().getValue(DispenserBlock.FACING);
            Vec3 direction = new Vec3(blockDirection.getStepX(),
                blockDirection.getStepY(),
                blockDirection.getStepY());

            Vec3 origin = blockSource.center();
            direction = GunItem.addSpread(direction, level.getRandom(), Config.dispenserBulletStdDev);

            BulletEntity bullet = new BulletEntity(level);
            bullet.setPos(origin.add(direction.scale(0.5)));
            bullet.setVelocity(Config.dispenserBulletSpeed, direction);
            bullet.damage = Config.dispenserDamage;

            level.addFreshEntity(bullet);

            level.playSound(null, origin.x(), origin.y(), origin.z(), Sounds.DISPENSER_FIRE, SoundSource.BLOCKS, 2.5f, 1);
            MusketMod.sendSmokeEffect(level, origin, direction);

            stack.shrink(1);
            return stack;
        }
    };
}
