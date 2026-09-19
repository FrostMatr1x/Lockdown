package com.frost.lockdown.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frost.lockdown.PermissionCheck;
import com.frost.lockdown.PermissionCheck.LockType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerMenu.class)
public class ContainerClickMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ItemLockerMixin");

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void onContainerClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

        if (slotId >= 0 && slotId < menu.slots.size()) {
            Slot slot = menu.getSlot(slotId);
            ItemStack stack = slot.getItem();
            if (handleClickLock(stack, player, ci)) {
                return;
            }
        }

        if (clickType == ClickType.SWAP && (button >= 0 && button <= 8 || button == 40)) {
            ItemStack hotbarStack = player.getInventory().getItem(button);
            LockType locktype = PermissionCheck.checkLock(hotbarStack, player, false);
            if (locktype != LockType.UNLOCK) {
                ci.cancel();
            }
        }
    }

    private boolean handleClickLock(ItemStack stack, Player player, CallbackInfo ci) {
        LockType locktype = PermissionCheck.checkLock(stack, player, false);

        if (locktype == LockType.ANNIHILATION) {
            stack.setCount(0);

            player.displayClientMessage(
                Component.literal("Этот предмет запрещен к использованию на сервере!").withStyle(ChatFormatting.RED),
                true
            );

            LOGGER.warn("Player {} tried to get an item that is not allowed on the server. Item {}", player.getName(), stack.getDisplayName());
            return true;
        }

        if (locktype == LockType.ERROR) {
            ci.cancel();

            player.displayClientMessage(
                Component.literal("У вас отсутствует скорборд, обратитесь к тех. админам.").withStyle(ChatFormatting.RED),
                true
            );
            LOGGER.error("Player {} don't have base scoreboard", player.getName().getString());

            return true;
        }

        if (locktype == LockType.LOCKED) {
            ci.cancel();

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
            return true;
        }

        if (locktype == LockType.CLICK_BLOCKED) {
            ci.cancel();

            player.displayClientMessage(
                Component.literal("Этот предмет заблокирован на сервере!").withStyle(ChatFormatting.RED),
                true
            );
            return true;
        }

        if (locktype == LockType.NBT_LOCKED) {
            ci.cancel();

            player.displayClientMessage(
                Component.literal("Тег блокировки удалён с предмета").withStyle(ChatFormatting.RED),
                true
            );
            return true;
        }

        return false;
    }
}
