package com.frost.lockdown;

import org.slf4j.Logger;

import com.frost.lockdown.PermissionCheck.LockType;
import com.mojang.logging.LogUtils;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public class GameEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        ItemEntity itemEntity = event.getItemEntity();
        ItemStack stack = itemEntity.getItem();
        Player player = event.getPlayer();

        LockType locktype = PermissionCheck.checkLock(stack, player, false);
        String itemId = PermissionCheck.getItemId(stack);

        if (locktype.equals(LockType.ANNIHILATION))
        {
            stack.setCount(0);

            Messages.send(player, Config.MSG_ITEM_PROHIBITED, "item", itemId);

            LOGGER.warn("Player {} tried to get an item that is not allowed on the server. Item {}", player.getName(), stack.getDisplayName());
            return;
        }

        if (locktype.equals(LockType.ERROR)) {

            event.setCanPickup(TriState.FALSE);

            Messages.send(player, Config.MSG_ITEM_SCOREBOARD_ERROR);
            LOGGER.warn("Player {} don't have base scoreboard", player.getName().getString());

            return;
        }

        if (locktype.equals(LockType.LOCKED)) {
            event.setCanPickup(TriState.FALSE);
            int level = PermissionCheck.getRequiredLevel(stack);

            Messages.send(player, Config.MSG_ITEM_LEVEL_LOCKED,
                    "item", itemId, "level", level, "score", PermissionCheck.getPlayerLevel(player));
        }

        if (locktype.equals(LockType.CLICK_BLOCKED)) {
            event.setCanPickup(TriState.FALSE);

            Messages.send(player, Config.MSG_ITEM_BLOCKED, "item", itemId);
        }

        if (locktype.equals(LockType.NBT_LOCKED)) {
            event.setCanPickup(TriState.FALSE);

            Messages.send(player, Config.MSG_ITEM_NBT, "item", itemId);
        }
    }
}
