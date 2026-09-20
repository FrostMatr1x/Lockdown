package com.frost.lockdown.compat;

import com.frost.lockdown.PermissionCheck;
import com.frost.lockdown.PermissionCheck.LockType;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;

public final class SophisticatedBackpacksCompat {

    private SophisticatedBackpacksCompat() {
    }

    public static LockType checkLock(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof BackpackItem)) {
            return LockType.UNLOCK;
        }
        return checkBackpack(stack, player);
    }

    private static LockType checkBackpack(ItemStack stack, Player player) {
        LockType result = LockType.UNLOCK;
        BackpackWrapper wrapper = new BackpackWrapper(stack);
        InventoryHandler inventory = wrapper.getInventoryHandler();

        int slots = inventory.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if (itemstack.isEmpty()) {
                continue;
            }

            LockType lockType = PermissionCheck.checkLock(itemstack, player, true);

            if (lockType == LockType.ANNIHILATION) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                continue;
            }

            if (lockType == LockType.NBT_LOCKED) {
                inventory.setStackInSlot(i, itemstack);
                result = LockType.NBT_LOCKED;
                continue;
            }

            if (lockType == LockType.CLICK_BLOCKED) {
                if (result == LockType.UNLOCK || result == LockType.LOCKED) {
                    result = LockType.CLICK_BLOCKED;
                }
                continue;
            }

            if (lockType == LockType.LOCKED) {
                if (result == LockType.UNLOCK) {
                    result = LockType.LOCKED;
                }
            }
        }

        return result;
    }
}
