package ewewukek.musketmod;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ClientUtilities {
    public static HumanoidModel.ArmPose getArmPose(Player player, InteractionHand hand) {
        if (player.swinging) return null;

        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.getItem() instanceof GunItem) {
            GunItem gunItem = (GunItem)stack.getItem();
            if (gunItem.canUseFrom(player, hand) && GunItem.isLoaded(stack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
        }
        return null;
    }
}
