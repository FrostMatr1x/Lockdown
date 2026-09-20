package com.frost.lockdown.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.frost.lockdown.Config;
import com.frost.lockdown.Messages;
import com.frost.lockdown.PermissionCheck;
import com.frost.lockdown.PermissionCheck.LockType;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

@Mixin(PlayerMainInvWrapper.class)
public class ItemHandlerInsertMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("ItemLockerMixin");
    @Shadow @Final private Inventory inventoryPlayer;

    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void itemlocker$onInsert(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        Player player = this.inventoryPlayer.player;

        LockType locktype = PermissionCheck.checkLock(stack, player, false);
        String itemId = PermissionCheck.getItemId(stack);

        if (locktype == LockType.ANNIHILATION)
        {
            stack.setCount(0);
            cir.setReturnValue(stack);

            Messages.send(player, Config.MSG_ITEM_PROHIBITED, "item", itemId);

            LOGGER.warn("Player {} tried to get an item that is not allowed on the server. Item {}", player.getName(), stack.getDisplayName());
            return;
        }

        if (locktype == LockType.ERROR) {

            cir.cancel();
            cir.setReturnValue(stack);

            Messages.send(player, Config.MSG_ITEM_SCOREBOARD_ERROR);
            LOGGER.error("Player {} don't have base scoreboard", player.getName().getString());

            return;
        }

        if (locktype == LockType.LOCKED) {
            cir.cancel();
            cir.setReturnValue(stack);

            int level = PermissionCheck.getRequiredLevel(stack);

            Messages.send(player, Config.MSG_ITEM_LEVEL_LOCKED,
                    "item", itemId, "level", level, "score", PermissionCheck.getPlayerLevel(player));
            return;
        }

        if (locktype == LockType.CLICK_BLOCKED) {
            cir.cancel();
            cir.setReturnValue(stack);

            Messages.send(player, Config.MSG_ITEM_BLOCKED, "item", itemId);
            return;
        }

        if (locktype == LockType.NBT_LOCKED) {
            cir.cancel();
            cir.setReturnValue(stack);

            Messages.send(player, Config.MSG_ITEM_NBT, "item", itemId);
        }
    }
}
