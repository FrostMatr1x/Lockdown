package com.frost.lockdown.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.frost.lockdown.PermissionCheck;
import com.frost.lockdown.PermissionCheck.LockType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Inventory.class)
public class InventoryAddMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ItemLockerMixin");

    @Shadow @Final public Player player;

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void preventItemAdd(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {

        LockType locktype = PermissionCheck.checkLock(stack, player, false);

        if (locktype == LockType.ANNIHILATION)
        {
            stack.setCount(0);
            cir.setReturnValue(false);

            player.displayClientMessage(
                Component.literal("Этот предмет запрещен к использованию на сервере!").withStyle(ChatFormatting.RED),
                true
            );

            LOGGER.warn("Player {} tried to get an item that is not allowed on the server. Item {}", player.getName(), stack.getDisplayName());
            return;
        }

        if (locktype == LockType.ERROR) {

            cir.cancel();
            cir.setReturnValue(false);

            player.displayClientMessage(
                Component.literal("У вас отсутствует скорборд, обратитесь к тех. админам.").withStyle(ChatFormatting.RED),
                true
            );
            LOGGER.error("Player {} don't have base scoreboard", player.getName().getString());

            return;
        }

        if (locktype == LockType.LOCKED) {
            cir.cancel();
            cir.setReturnValue(false);

            int level = PermissionCheck.getRequiredLevel(stack);
            String levelMessage = "Этот предмет заблокирован на данном уровне! ";

            if (level != -1)
            {
                levelMessage += "Требуемый уровень " + level;
            }

            player.displayClientMessage(
                Component.literal(levelMessage).withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        if (locktype == LockType.CLICK_BLOCKED) {
            cir.cancel();
            cir.setReturnValue(false);

            player.displayClientMessage(
                Component.literal("Этот предмет заблокирован на сервере!").withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        if (locktype == LockType.NBT_LOCKED) {
            cir.cancel();
            cir.setReturnValue(false);

            player.displayClientMessage(
                Component.literal("Тег блокировки удалён с предмета").withStyle(ChatFormatting.RED),
                true
            );
        }
    }
}
