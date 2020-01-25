package ewewukek.musketmod;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

public class MusketItem extends Item {
    public static final int DURABILITY = 250;
    public static final int LOADING_STAGE_1 = 5;
    public static final int LOADING_STAGE_2 = 10;
    public static final int LOADING_STAGE_3 = 20;
    public static final int RELOAD_DURATION = 30;
    public static final float DISPERSION_STD = (float)Math.toRadians(1);

    private int loadingStage;

    @ObjectHolder(MusketMod.MODID + ":cartridge")
    public static Item CARTRIDGE;

    @ObjectHolder(MusketMod.MODID + ":musket_load0")
    public static SoundEvent SOUND_MUSKET_LOAD_0;
    @ObjectHolder(MusketMod.MODID + ":musket_load1")
    public static SoundEvent SOUND_MUSKET_LOAD_1;
    @ObjectHolder(MusketMod.MODID + ":musket_load2")
    public static SoundEvent SOUND_MUSKET_LOAD_2;

    @ObjectHolder(MusketMod.MODID + ":musket_ready")
    public static SoundEvent SOUND_MUSKET_READY;

    @ObjectHolder(MusketMod.MODID + ":musket_fire")
    public static SoundEvent SOUND_MUSKET_FIRE;

    public MusketItem(Item.Properties properties) {
        super(properties.defaultMaxDamage(DURABILITY));

        addPropertyOverride(new ResourceLocation("loaded"), (stack, world, player) -> {
            return isLoaded(stack) ? 1 : 0;
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) return super.onItemRightClick(worldIn, player, hand);

        ItemStack stack = player.getHeldItem(hand);
        boolean creative = player.abilities.isCreativeMode;

        if (player.areEyesInFluid(FluidTags.WATER) && !creative) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }

        boolean haveAmmo = !findAmmo(player).isEmpty() || creative;
        boolean loaded = isLoaded(stack);

        if (loaded && isReady(stack)) {
            if (!worldIn.isRemote) {
                fireBullet(worldIn, player);

            } else {
                fireParticles(worldIn, player);
            }
            player.playSound(SOUND_MUSKET_FIRE, 1.5f, 1);

            damageItem(stack, player);
            setReady(stack, false);
            setLoaded(stack, false);

            return new ActionResult<>(ActionResultType.SUCCESS, stack);

        } else if (loaded || haveAmmo) {

            if (!loaded) loadingStage = 0;
            player.setActiveHand(hand);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);

        } else {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (isLoaded(stack)) setReady(stack, true);
    }

    // called by LivingEntity.updateActiveHand
    @Override
    public void func_219972_a(World world, LivingEntity entity, ItemStack stack, int timeLeft) {
        if (world.isRemote || !(entity instanceof PlayerEntity)) return;

        int usingDuration = getUseDuration(stack) - timeLeft;

        double posX = entity.getPosX();
        double posY = entity.getPosY();
        double posZ = entity.getPosZ();

        if (loadingStage == 0 && usingDuration >= LOADING_STAGE_1) {
            world.playSound(null, posX, posY, posZ, SOUND_MUSKET_LOAD_0, SoundCategory.PLAYERS, 0.5F, 1.0F);
            loadingStage = 1;

        } else if (loadingStage == 1 && usingDuration >= LOADING_STAGE_2) {
            world.playSound(null, posX, posY, posZ, SOUND_MUSKET_LOAD_1, SoundCategory.PLAYERS, 0.5F, 1.0F);
            loadingStage = 2;

        } else if (loadingStage == 2 && usingDuration >= LOADING_STAGE_3) {
            world.playSound(null, posX, posY, posZ, SOUND_MUSKET_LOAD_2, SoundCategory.PLAYERS, 0.5F, 1.0F);
            loadingStage = 3;
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof PlayerEntity)) return;

        if (getUseDuration(stack) - timeLeft >= RELOAD_DURATION && !isLoaded(stack)) {
            PlayerEntity player = (PlayerEntity)entityLiving;

            if (!player.abilities.isCreativeMode) {
                ItemStack ammoStack = findAmmo(player);
                if (ammoStack.isEmpty()) return;

                ammoStack.shrink(1);
                if (ammoStack.isEmpty()) player.inventory.deleteStack(ammoStack);
            }

            player.playSound(SOUND_MUSKET_READY, 0.5f, 1);
            setLoaded(stack, true);
        }
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (entityLiving instanceof PlayerEntity) {
            damageItem(stack, (PlayerEntity)entityLiving);
        }
        return false;
    }

    public static void damageItem(ItemStack stack, PlayerEntity player) {
        stack.damageItem(1, player, (entity) -> {
            entity.sendBreakAnimation(player.getActiveHand());
        });
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public static boolean isLoaded(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.getByte("loaded") == 1;
    }

    public static boolean isReady(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.getByte("ready") == 1;
    }

    private boolean isAmmo(ItemStack stack) {
        return stack.getItem() == CARTRIDGE;
    }

    private ItemStack findAmmo(PlayerEntity player) {
        if (isAmmo(player.getHeldItem(Hand.OFF_HAND))) {
            return player.getHeldItem(Hand.OFF_HAND);

        } else if (isAmmo(player.getHeldItem(Hand.MAIN_HAND))) {
            return player.getHeldItem(Hand.MAIN_HAND);

        } else {
            for (int i = 0; i != player.inventory.getSizeInventory(); ++i) {
                ItemStack itemstack = player.inventory.getStackInSlot(i);
                if (isAmmo(itemstack)) return itemstack;
            }

            return ItemStack.EMPTY;
        }
    }

    private Vec3d getPlayerFiringPoint(PlayerEntity player) {
        Vec3d side = Vec3d.fromPitchYaw(0, player.rotationYaw + 90);
        if (player.getActiveHand() == Hand.OFF_HAND) side = side.scale(-1);
        Vec3d down = Vec3d.fromPitchYaw(player.rotationPitch + 90, player.rotationYaw);

        double posX = player.getPosX();
        double posY = player.getPosY();
        double posZ = player.getPosZ();

        return new Vec3d(posX, posY + player.getEyeHeight(), posZ)
                    .add(side.add(down).scale(0.1));
    }

    private void fireBullet(World worldIn, PlayerEntity player) {
        Vec3d pos = getPlayerFiringPoint(player);
        Vec3d front = Vec3d.fromPitchYaw(player.rotationPitch, player.rotationYaw);

        float angle = (float)Math.PI * 2 * random.nextFloat();
        float gaussian = Math.abs((float)random.nextGaussian());
        if (gaussian > 4) gaussian = 4;

        front = front.rotatePitch(DISPERSION_STD * gaussian * MathHelper.sin(angle))
                       .rotateYaw(DISPERSION_STD * gaussian * MathHelper.cos(angle));

        Vec3d motion = front.scale(BulletEntity.VELOCITY);

        Vec3d playerMotion = player.getMotion();
        motion.add(playerMotion.x, player.onGround ? 0 : playerMotion.y, playerMotion.z);

        BulletEntity bullet = new BulletEntity(worldIn);
        bullet.shooterUuid = player.getUniqueID();
        bullet.setPosition(pos.x, pos.y, pos.z);
        bullet.setMotion(motion);

        worldIn.addEntity(bullet);
    }

    private void fireParticles(World world, PlayerEntity player) {
        Vec3d pos = getPlayerFiringPoint(player);
        Vec3d front = Vec3d.fromPitchYaw(player.rotationPitch, player.rotationYaw);

        for (int i = 0; i != 10; ++i) {
            float t = random.nextFloat();
            Vec3d p = pos.add(front.scale(0.5 + t));
            Vec3d v = front.scale(0.1).scale(1 - t);
            world.addParticle(ParticleTypes.POOF, p.x, p.y, p.z, v.x, v.y, v.z);
        }
    }

    private void setLoaded(ItemStack stack, boolean loaded) {
        stack.getOrCreateTag().putByte("loaded", (byte)(loaded ? 1 : 0));
    }

    private void setReady(ItemStack stack, boolean ready) {
        stack.getOrCreateTag().putByte("ready", (byte)(ready ? 1 : 0));
    }
}
