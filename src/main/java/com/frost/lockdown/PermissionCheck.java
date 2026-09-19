package com.frost.lockdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;

public class PermissionCheck {
    private static final Set<TagKey<Item>> CHESTS_TAG = Set.of(
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "chests")),
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "shulker_boxes"))
    );

    public enum LockType {
        ANNIHILATION,
        CLICK_BLOCKED,
        NBT_LOCKED,
        LOCKED,
        UNLOCK,
        ERROR
    }

    private record ItemLockKey(Item item, int playerLevel, int configVersion) {}

    private static final Map<ItemLockKey, LockType> DETERMINISTIC_CACHE = new ConcurrentHashMap<>();
    private static volatile int cacheConfigVersion = -1;

    public static LockType checkLock(ItemStack stack, Player player, boolean iteration) {
        if (stack.isEmpty() || isBypassed(player)) {
            return LockType.UNLOCK;
        }

        Item item = stack.getItem();
        int playerLevel = resolvePlayerLevel(player);

        LockType deterministic = getDeterministicLock(player, item, playerLevel);

        if (deterministic == LockType.ANNIHILATION || deterministic == LockType.CLICK_BLOCKED) {
            if (deterministic == LockType.ANNIHILATION) {
                JsonStorageManager.addEntry(player.getGameProfile().getName(), getItemId(item), stack.getCount());
            }
            return deterministic;
        }

        if (checkNbtTag(stack)) {
            return LockType.NBT_LOCKED;
        }

        if (deterministic == LockType.ERROR) {
            return LockType.ERROR;
        }

        if (!iteration) {
            for (TagKey<Item> tag : CHESTS_TAG) {
                if (stack.is(tag)) {
                    LockType containerResult = checkContainer(stack, player);
                    if (containerResult != LockType.UNLOCK) {
                        return containerResult;
                    }
                    break;
                }
            }

            if (item instanceof BackpackItem) {
                LockType backpackResult = checkBackpack(stack, player);
                if (backpackResult != LockType.UNLOCK) {
                    return backpackResult;
                }
            }
        }

        if (deterministic == LockType.LOCKED) {
            return LockType.LOCKED;
        }

        return LockType.UNLOCK;
    }

    public static LockType checkLockForDispenser(ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return LockType.UNLOCK;
        }

        Item item = stack.getItem();
        ResourceLocation location = BuiltInRegistries.ITEM.getKey(item);
        String itemId = location.toString();

        if (Config.getProhibitedItemIdsSet().contains(itemId)) {
            return LockType.ANNIHILATION;
        }
        if (Config.getBlockedItemIdsSet().contains(itemId)) {
            return LockType.CLICK_BLOCKED;
        }
        if (checkNbtTag(stack)) {
            return LockType.NBT_LOCKED;
        }

        if (player == null) {
            return LockType.UNLOCK;
        }

        return checkLevelLock(itemId, location.getNamespace(), resolvePlayerLevel(player));
    }

    private static boolean isBypassed(Player player) {
        return player.isCreative() || player.hasPermissions(2);
    }

    private static int resolvePlayerLevel(Player player) {
        OptionalInt levelOpt = ScoreboardHandler.tryGetScoreboardValue(Config.SCOREBOARD_NAME.get(), player);
        return levelOpt.orElse(-1);
    }

    private static LockType getDeterministicLock(Player player, Item item, int playerLevel) {
        boolean serverSide = !player.level().isClientSide();
        if (!serverSide) {
            return computeDeterministicLock(item, playerLevel, false);
        }

        int version = Config.getConfigVersion();
        if (cacheConfigVersion != version) {
            DETERMINISTIC_CACHE.clear();
            cacheConfigVersion = version;
        }

        ItemLockKey key = new ItemLockKey(item, playerLevel, version);
        LockType cached = DETERMINISTIC_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        LockType result = computeDeterministicLock(item, playerLevel, true);
        DETERMINISTIC_CACHE.put(key, result);
        return result;
    }

    private static LockType computeDeterministicLock(Item item, int playerLevel, boolean serverSide) {
        ResourceLocation location = BuiltInRegistries.ITEM.getKey(item);
        String itemId = location.toString();
        String modId = location.getNamespace();

        if (Config.getProhibitedItemIdsSet().contains(itemId)) {
            return LockType.ANNIHILATION;
        }
        if (Config.getBlockedItemIdsSet().contains(itemId)) {
            return LockType.CLICK_BLOCKED;
        }

        if (playerLevel == -1) {
            if (!serverSide) {
                return LockType.UNLOCK;
            }
            if (Config.getItemLevelIdsMap().containsKey(itemId) || Config.getModLevelIdsMap().containsKey(modId)) {
                return LockType.ERROR;
            }
            return LockType.UNLOCK;
        }

        return checkLevelLock(itemId, modId, playerLevel);
    }

    private static LockType checkLevelLock(String itemId, String modId, int playerLevel) {
        if (playerLevel < 0) {
            return LockType.UNLOCK;
        }

        Map<String, Integer> modLevelIds = Config.getModLevelIdsMap();
        Integer modReq = modLevelIds.get(modId);

        if (modReq != null && modReq > playerLevel) {
            if (Config.getModExcludeIdsSet().contains(itemId)) {
                return LockType.UNLOCK;
            }
            return LockType.LOCKED;
        }

        Map<String, Integer> itemsLevelIds = Config.getItemLevelIdsMap();
        Integer itemReq = itemsLevelIds.get(itemId);

        if (itemReq != null && itemReq > playerLevel) {
            return LockType.LOCKED;
        }

        return LockType.UNLOCK;
    }

    private static boolean checkNbtTag(ItemStack stack) {
        String tag = Config.getLockNbtTag();
        if (tag == null || tag.isEmpty()) {
            return false;
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!customData.contains(tag)) {
            return false;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, compoundTag -> compoundTag.remove(tag));
        return true;
    }

    private static String getItemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static LockType checkContainer(ItemStack stack, Player player) {
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null) {
            return LockType.UNLOCK;
        }

        List<ItemStack> validItems = null;
        boolean modified = false;
        LockType result = LockType.UNLOCK;

        int index = 0;
        for (ItemStack item : contents.nonEmptyItems()) {
            LockType lockType = checkLock(item, player, true);

            if (lockType == LockType.ANNIHILATION) {
                if (!modified) {
                    modified = true;
                    validItems = copyPreviousItems(contents.nonEmptyItems(), index);
                }
                JsonStorageManager.addEntry(player.getGameProfile().getName(), getItemId(item.getItem()), item.getCount());
                index++;
                continue;
            }

            if (modified) {
                validItems.add(item);
            }

            if (lockType == LockType.NBT_LOCKED) {
                if (!modified) {
                    modified = true;
                    validItems = copyPreviousItems(contents.nonEmptyItems(), index + 1);
                }
                result = LockType.NBT_LOCKED;
            } else if (lockType == LockType.CLICK_BLOCKED) {
                if (result == LockType.UNLOCK || result == LockType.LOCKED) {
                    result = LockType.CLICK_BLOCKED;
                }
            } else if (lockType == LockType.LOCKED) {
                if (result == LockType.UNLOCK) {
                    result = LockType.LOCKED;
                }
            }

            index++;
        }

        if (modified && validItems != null) {
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(validItems));
        }

        return result;
    }

    private static List<ItemStack> copyPreviousItems(Iterable<ItemStack> items, int count) {
        List<ItemStack> list = new ArrayList<>(count);
        int i = 0;
        for (ItemStack item : items) {
            if (i >= count) break;
            list.add(item);
            i++;
        }
        return list;
    }

    private static LockType checkBackpack(ItemStack stack, Player player) {
        LockType result = LockType.UNLOCK;
        BackpackWrapper wrapper = new BackpackWrapper(stack);
        IItemHandlerModifiable inventory = wrapper.getInventoryHandler();

        int slots = inventory.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if (itemstack.isEmpty()) {
                continue;
            }

            LockType lockType = checkLock(itemstack, player, true);

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

    public static int getRequiredLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }

        ResourceLocation location = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemId = location.toString();

        Map<String, Integer> itemLevels = Config.getItemLevelIdsMap();
        Integer itemLevel = itemLevels.get(itemId);
        if (itemLevel != null) {
            return itemLevel;
        }

        String modId = location.getNamespace();
        Map<String, Integer> modLevels = Config.getModLevelIdsMap();
        Integer modLevel = modLevels.get(modId);
        if (modLevel != null) {
            return modLevel;
        }

        return -1;
    }
}