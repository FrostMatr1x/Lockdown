package com.frost.lockdown.compat;

import com.frost.lockdown.PermissionCheck.LockType;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BackpackScanner {

    public interface Scanner {
        boolean isAvailable();

        LockType checkLock(ItemStack stack, Player player);
    }

    private static final Scanner NONE = new Scanner() {
        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public LockType checkLock(ItemStack stack, Player player) {
            return LockType.UNLOCK;
        }
    };

    private static volatile Scanner current = NONE;

    private BackpackScanner() {
    }

    public static void register(Scanner scanner) {
        current = scanner != null ? scanner : NONE;
    }

    public static boolean isAvailable() {
        return current.isAvailable();
    }

    public static LockType checkLock(ItemStack stack, Player player) {
        return current.checkLock(stack, player);
    }
}
