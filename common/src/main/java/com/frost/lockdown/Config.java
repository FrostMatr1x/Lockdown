package com.frost.lockdown;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.frost.lockdown.config.ConfigAccess;
import com.frost.lockdown.config.DeferredSetting;
import com.frost.lockdown.config.Setting;
import com.frost.lockdown.effect.EffectLockerConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class Config {

    public static final Setting<List<String>> PROHIBITED_ITEM_IDS = deferred("itemLocker.prohibitedItemIds", ConfigAccess::prohibitedItemIds);
    public static final Setting<List<String>> BLOCKED_ITEM_IDS = deferred("itemLocker.blockedItemIds", ConfigAccess::blockedItemIds);
    public static final Setting<List<String>> EXCLUDE_ITEM_IDS = deferred("itemLocker.excludeItemIds", ConfigAccess::excludeItemIds);
    public static final Setting<List<String>> ITEM_LEVELS_RAW = deferred("itemLocker.itemLevels", ConfigAccess::itemLevels);
    public static final Setting<List<String>> MOD_LEVELS_RAW = deferred("itemLocker.modLevels", ConfigAccess::modLevels);
    public static final Setting<String> SCOREBOARD_NAME = deferred("itemLocker.scoreboard", ConfigAccess::scoreboardName);
    public static final Setting<String> LOCK_NBT_TAG = deferred("itemLocker.lockNbtTag", ConfigAccess::lockNbtTag);

    public static final Setting<String> MSG_ITEM_PROHIBITED = deferred("itemLocker.messages.itemProhibitedMessage", ConfigAccess::msgItemProhibited);
    public static final Setting<String> MSG_ITEM_BLOCKED = deferred("itemLocker.messages.itemBlockedMessage", ConfigAccess::msgItemBlocked);
    public static final Setting<String> MSG_ITEM_LEVEL_LOCKED = deferred("itemLocker.messages.itemLevelLockedMessage", ConfigAccess::msgItemLevelLocked);
    public static final Setting<String> MSG_ITEM_SCOREBOARD_ERROR = deferred("itemLocker.messages.itemScoreboardErrorMessage", ConfigAccess::msgItemScoreboardError);
    public static final Setting<String> MSG_ITEM_NBT = deferred("itemLocker.messages.itemNbtMessage", ConfigAccess::msgItemNbt);

    public static final Setting<List<String>> EFFECT_BANNED = deferred("effectLocker.bannedEffects", ConfigAccess::effectBanned);
    public static final Setting<List<String>> EFFECT_CAPPED = deferred("effectLocker.cappedEffects", ConfigAccess::effectCapped);
    public static final Setting<List<String>> EFFECT_BYPASS_ITEMS = deferred("effectLocker.effectBypassItems", ConfigAccess::effectBypassItems);

    public static final Setting<String> MSG_EFFECT_BLOCKED = deferred("effectLocker.messages.effectBlockedMessage", ConfigAccess::msgEffectBlocked);
    public static final Setting<String> MSG_EFFECT_CAPPED = deferred("effectLocker.messages.effectCappedMessage", ConfigAccess::msgEffectCapped);
    public static final Setting<String> MSG_ITEM_USE_BLOCKED = deferred("effectLocker.messages.itemUseBlockedMessage", ConfigAccess::msgItemUseBlocked);
    public static final Setting<String> MSG_EFFECT_BANNED_CHAT = deferred("effectLocker.messages.effectBannedChat", ConfigAccess::msgEffectBannedChat);
    public static final Setting<String> MSG_EFFECT_UNBANNED_CHAT = deferred("effectLocker.messages.effectUnbannedChat", ConfigAccess::msgEffectUnbannedChat);
    public static final Setting<String> MSG_EFFECT_CAPPED_CHAT = deferred("effectLocker.messages.effectCappedChat", ConfigAccess::msgEffectCappedChat);
    public static final Setting<String> MSG_EFFECT_CAP_REMOVED_CHAT = deferred("effectLocker.messages.effectCapRemovedChat", ConfigAccess::msgEffectCapRemovedChat);
    public static final Setting<String> MSG_EFFECT_CLEARED_CHAT = deferred("effectLocker.messages.effectClearedChat", ConfigAccess::msgEffectClearedChat);
    public static final Setting<String> MSG_BYPASS_ADDED_CHAT = deferred("effectLocker.messages.bypassAddedChat", ConfigAccess::msgBypassAddedChat);
    public static final Setting<String> MSG_BYPASS_REMOVED_CHAT = deferred("effectLocker.messages.bypassRemovedChat", ConfigAccess::msgBypassRemovedChat);

    private static volatile Set<String> prohibitedItemIdsCache = Set.of();
    private static volatile Set<String> blockedItemIdsCache = Set.of();
    private static volatile Set<String> excludeItemIdsCache = Set.of();
    private static volatile Map<String, Integer> itemLevelIdsCache = new HashMap<>();
    private static volatile Map<String, Integer> modLevelIdsCache = new HashMap<>();
    private static volatile int configVersion = 0;

    private Config() {
    }

    private static <T> Setting<T> deferred(String id, java.util.function.Function<ConfigAccess, Setting<T>> resolver) {
        return new DeferredSetting<>(id, resolver);
    }

    public static boolean validateItemName(final Object obj) {
        if (!(obj instanceof String itemName)) return false;
        try {
            return BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateEffectName(final Object obj) {
        if (!(obj instanceof String effectName)) return false;
        try {
            return BuiltInRegistries.MOB_EFFECT.containsKey(ResourceLocation.parse(effectName));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateEffectCapString(final Object obj) {
        if (!(obj instanceof String str)) return false;
        String[] parts = str.split("=", 2);
        if (parts.length != 2) return false;
        try {
            if (!BuiltInRegistries.MOB_EFFECT.containsKey(ResourceLocation.parse(parts[0].trim()))) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return parseAmplifier(parts[1].trim()) >= 0;
    }

    public static boolean validateEffectBypassString(final Object obj) {
        return obj instanceof String str && EffectLockerConfig.isValidBypassEntry(str);
    }

    public static boolean validateScoreboardName(final Object obj) {
        return true;
    }

    public static boolean validateItemLevelString(final Object obj) {
        if (!(obj instanceof String str)) return false;
        String[] parts = str.split("=");
        if (parts.length != 2) return false;
        try {
            if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(parts[0].trim()))) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        try {
            Integer.parseInt(parts[1].trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean validateModLevelString(final Object obj) {
        if (!(obj instanceof String str)) return false;
        String[] parts = str.split("=");
        if (parts.length != 2) return false;
        try {
            Integer.parseInt(parts[1].trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int parseAmplifier(String value) {
        try {
            int amplifier = Integer.parseInt(value);
            return (amplifier < 0 || amplifier > 255) ? -1 : amplifier;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static Set<String> getProhibitedItemIdsSet() {
        return prohibitedItemIdsCache;
    }

    public static Map<String, Integer> getItemLevelIdsMap() {
        return itemLevelIdsCache;
    }

    public static Map<String, Integer> getModLevelIdsMap() {
        return modLevelIdsCache;
    }

    public static Set<String> getModExcludeIdsSet() {
        return excludeItemIdsCache;
    }

    public static Set<String> getBlockedItemIdsSet() {
        return blockedItemIdsCache;
    }

    public static String getLockNbtTag() {
        return LOCK_NBT_TAG.get();
    }

    public static int getConfigVersion() {
        return configVersion;
    }

    public static void rebuildProhibitedItemIdsCache() {
        prohibitedItemIdsCache = new HashSet<>(PROHIBITED_ITEM_IDS.get());
        configVersion++;
    }

    public static void rebuildBlockedItemIdsCache() {
        blockedItemIdsCache = new HashSet<>(BLOCKED_ITEM_IDS.get());
        configVersion++;
    }

    public static void rebuildExcludeItemIdsCache() {
        excludeItemIdsCache = new HashSet<>(EXCLUDE_ITEM_IDS.get());
        configVersion++;
    }

    public static void rebuildItemLevelIdsCache() {
        Map<String, Integer> rebuilt = new HashMap<>();
        for (var itemAndLevel : ITEM_LEVELS_RAW.get()) {
            String[] split = itemAndLevel.split("=", 2);
            rebuilt.put(split[0].trim(), Integer.parseInt(split[1].trim()));
        }
        itemLevelIdsCache = rebuilt;
        configVersion++;
    }

    public static void rebuildModLevelIdsCache() {
        Map<String, Integer> rebuilt = new HashMap<>();
        for (var modAndLevel : MOD_LEVELS_RAW.get()) {
            String[] split = modAndLevel.split("=", 2);
            rebuilt.put(split[0].trim(), Integer.parseInt(split[1].trim()));
        }
        modLevelIdsCache = rebuilt;
        configVersion++;
    }

    public static void rebuildAll() {
        rebuildProhibitedItemIdsCache();
        rebuildBlockedItemIdsCache();
        rebuildExcludeItemIdsCache();
        rebuildItemLevelIdsCache();
        rebuildModLevelIdsCache();
        EffectLockerConfig.rebuild();
    }
}
