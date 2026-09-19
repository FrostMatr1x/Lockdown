package com.frost.lockdown.mixin;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.frost.lockdown.PermissionCheck;
import com.frost.lockdown.PermissionCheck.LockType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

@Mixin(ArmorItem.class)
public class ArmorItemDispenseMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ItemLockerMixin");
    private static final double SEARCH_RADIUS = 8.0;

    @Inject(method = "dispenseArmor", at = @At("HEAD"), cancellable = true)
    private static void itemlocker$onDispenseArmor(BlockSource blockSource, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Player player = findNearestPlayer(blockSource);
        LockType lock = PermissionCheck.checkLockForDispenser(stack, player);

        if (lock == LockType.ANNIHILATION) {
            LOGGER.warn("Dispenser tried to equip a prohibited item. Item {}", stack.getDisplayName());
            stack.setCount(0);
            cir.setReturnValue(false);
            return;
        }

        if (lock != LockType.UNLOCK) {
            LOGGER.warn("Dispenser tried to equip a blocked item. Item {}", stack.getDisplayName());
            cir.setReturnValue(false);
        }
    }

    private static Player findNearestPlayer(BlockSource blockSource) {
        ServerLevel level = blockSource.level();
        BlockPos pos = blockSource.pos();

        AABB area = new AABB(pos).inflate(SEARCH_RADIUS);
        List<Player> players = level.getEntitiesOfClass(Player.class, area, p -> !p.isSpectator());
        if (players.isEmpty()) {
            return null;
        }

        Player nearest = players.get(0);
        double nearestDistSqr = nearest.distanceToSqr(pos.getCenter());
        for (int i = 1; i < players.size(); i++) {
            Player candidate = players.get(i);
            double distSqr = candidate.distanceToSqr(pos.getCenter());
            if (distSqr < nearestDistSqr) {
                nearestDistSqr = distSqr;
                nearest = candidate;
            }
        }

        return nearest;
    }
}
