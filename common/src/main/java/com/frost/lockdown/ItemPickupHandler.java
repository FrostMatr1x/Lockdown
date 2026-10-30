package com.frost.lockdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frost.lockdown.PermissionCheck.LockType;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ItemPickupHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("ItemLockerMixin");

    private ItemPickupHandler() {
    }

    public static boolean handle(ItemStack stack, Player player) {
        LockType locktype = PermissionCheck.checkLock(stack, player, false);
        String itemId = PermissionCheck.getItemId(stack);

        if (locktype == LockType.ANNIHILATION) {
            stack.setCount(0);

            Messages.send(player, Config.MSG_ITEM_PROHIBITED, "item", itemId);

            LOGGER.warn("Player {} tried to get an item that is not allowed on the server. Item {}", player.getName(), stack.getDisplayName());
            return true;
        }

        if (locktype == LockType.ERROR) {
            Messages.send(player, Config.MSG_ITEM_SCOREBOARD_ERROR);
            LOGGER.warn("Player {} don't have base scoreboard", player.getName().getString());
            return true;
        }

        if (locktype == LockType.LOCKED) {
            int level = PermissionCheck.getRequiredLevel(stack);

            Messages.send(player, Config.MSG_ITEM_LEVEL_LOCKED,
                    "item", itemId, "level", level, "score", PermissionCheck.getPlayerLevel(player));
            return true;
        }

        if (locktype == LockType.CLICK_BLOCKED) {
            Messages.send(player, Config.MSG_ITEM_BLOCKED, "item", itemId);
            return true;
        }

        if (locktype == LockType.NBT_LOCKED) {
            Messages.send(player, Config.MSG_ITEM_NBT, "item", itemId);
            return true;
        }

        return false;
    }
}
