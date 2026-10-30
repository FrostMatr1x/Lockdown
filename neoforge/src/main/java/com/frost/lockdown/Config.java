package com.frost.lockdown;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.frost.lockdown.effect.EffectLockerConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();


    public static final ModConfigSpec.ConfigValue<List<? extends String>> PROHIBITED_ITEM_IDS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_ITEM_IDS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDE_ITEM_IDS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_LEVELS_RAW;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_LEVELS_RAW;
    public static final ModConfigSpec.ConfigValue<String> SCOREBOARD_NAME;
    public static final ModConfigSpec.ConfigValue<String> LOCK_NBT_TAG;

    public static final ModConfigSpec.ConfigValue<String> MSG_ITEM_PROHIBITED;
    public static final ModConfigSpec.ConfigValue<String> MSG_ITEM_BLOCKED;
    public static final ModConfigSpec.ConfigValue<String> MSG_ITEM_LEVEL_LOCKED;
    public static final ModConfigSpec.ConfigValue<String> MSG_ITEM_SCOREBOARD_ERROR;
    public static final ModConfigSpec.ConfigValue<String> MSG_ITEM_NBT;


    public static final ModConfigSpec.ConfigValue<List<? extends String>> EFFECT_BANNED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EFFECT_CAPPED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EFFECT_BYPASS_ITEMS;

    public static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_BLOCKED;
    public static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_CAPPED;
    public static final ModConfigSpec.ConfigValue<String> MSG_ITEM_USE_BLOCKED;
    public static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_BANNED_CHAT;
    public static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_UNBANNED_CHAT;
    public static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_CAPPED_CHAT;
    public static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_CAP_REMOVED_CHAT;
    public static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_CLEARED_CHAT;
    public static final ModConfigSpec.ConfigValue<String> MSG_BYPASS_ADDED_CHAT;
    public static final ModConfigSpec.ConfigValue<String> MSG_BYPASS_REMOVED_CHAT;

    static {
        BUILDER.comment("ItemLocker module settings.").push("itemLocker");
        PROHIBITED_ITEM_IDS = BUILDER
                .comment("List of item IDs that are completely prohibited (destroyed upon attempt to acquire).")
                .defineListAllowEmpty("prohibitedItemIds", List.of(), Config::validateItemName);
        BLOCKED_ITEM_IDS = BUILDER
                .comment("List of item IDs that are blocked without being destroyed and without a JSON log.")
                .defineListAllowEmpty("blockedItemIds", List.of(), Config::validateItemName);
        EXCLUDE_ITEM_IDS = BUILDER
                .comment("List of excluded item IDs that are allowed at any level.")
                .defineListAllowEmpty("excludeItemIds", List.of(), Config::validateItemName);
        ITEM_LEVELS_RAW = BUILDER
                .comment("List of items and their required levels in the format 'modid:item_id=level'. Example: 'minecraft:diamond=5'")
                .defineListAllowEmpty("itemLevels", List.of(), Config::validateItemLevelString);
        MOD_LEVELS_RAW = BUILDER
                .comment("List of mods and their required levels in the format 'modid=level'. Example: 'minecraft=5'")
                .defineListAllowEmpty("modLevels", List.of(), Config::validateModLevelString);
        SCOREBOARD_NAME = BUILDER
                .comment("Name of the Scoreboard from which the player's level is retrieved.")
                .define("scoreboard", "", Config::validateScoreboardName);
        LOCK_NBT_TAG = BUILDER
                .comment("Name of the NBT tag in custom_data that locks the item until the tag is removed.")
                .define("lockNbtTag", "lockdown:locked");

        BUILDER.comment("ItemLocker module messages. Placeholders: %item%, %player%, %level%, %score%.").push("messages");
        MSG_ITEM_PROHIBITED = BUILDER
                .comment("Message when a prohibited item is destroyed. Placeholders: %item%")
                .define("itemProhibitedMessage", "Item %item% is prohibited on the server and was destroyed.");
        MSG_ITEM_BLOCKED = BUILDER
                .comment("Message when an item is blocked. Placeholders: %item%")
                .define("itemBlockedMessage", "Item %item% is blocked on the server.");
        MSG_ITEM_LEVEL_LOCKED = BUILDER
                .comment("Message when an item is locked by level. Placeholders: %item%, %level%, %score%")
                .define("itemLevelLockedMessage", "Item %item% is locked. Required level: %level%, your level: %score%.");
        MSG_ITEM_SCOREBOARD_ERROR = BUILDER
                .comment("Message when the scoreboard is missing.")
                .define("itemScoreboardErrorMessage", "You are missing the required scoreboard. Please contact server administration.");
        MSG_ITEM_NBT = BUILDER
                .comment("Message when the lock tag is removed. Placeholders: %item%")
                .define("itemNbtMessage", "The lock tag has been removed from item %item%.");
        BUILDER.pop();
        BUILDER.pop();

        BUILDER.comment("EffectLocker module settings.").push("effectLocker");
        EFFECT_BANNED = BUILDER
                .comment("List of completely banned effects in the format 'modid:effect'.")
                .defineListAllowEmpty("bannedEffects", List.of(), Config::validateEffectName);
        EFFECT_CAPPED = BUILDER
                .comment("List of capped effects in the format 'modid:effect=level'.")
                .defineListAllowEmpty("cappedEffects", List.of(), Config::validateEffectCapString);
        EFFECT_BYPASS_ITEMS = BUILDER
                .comment("Items allowed to apply the effect. Format: 'modid:effect|modid:item[;modid:item2...]'")
                .defineListAllowEmpty("effectBypassItems", List.of(), Config::validateEffectBypassString);

        BUILDER.comment("EffectLocker module messages. Placeholders: %item%, %player%, %effect%, %level%, %score%, %count%.").push("messages");
        MSG_EFFECT_BLOCKED = BUILDER
                .comment("Message when an effect is blocked. Placeholders: %effect%")
                .define("effectBlockedMessage", "Effect %effect% is prohibited on the server.");
        MSG_EFFECT_CAPPED = BUILDER
                .comment("Message when an effect level is capped. Placeholders: %effect%, %level%")
                .define("effectCappedMessage", "Effect %effect% is capped at level %level%.");
        MSG_ITEM_USE_BLOCKED = BUILDER
                .comment("Message when using an item that applies a prohibited effect. Placeholders: %item%, %effect%")
                .define("itemUseBlockedMessage", "Item %item% cannot apply effect %effect%.");
        MSG_EFFECT_BANNED_CHAT = BUILDER
                .comment("Message for command /lockdown effect ban. Placeholders: %effect%, %count%")
                .define("effectBannedChat", "Effect %effect% has been banned and removed from %count% players.");
        MSG_EFFECT_UNBANNED_CHAT = BUILDER
                .comment("Message for command /lockdown effect unban. Placeholders: %effect%")
                .define("effectUnbannedChat", "Effect %effect% has been unbanned.");
        MSG_EFFECT_CAPPED_CHAT = BUILDER
                .comment("Message for command /lockdown effect cap. Placeholders: %effect%, %level%")
                .define("effectCappedChat", "Max level for effect %effect% is set to %level%.");
        MSG_EFFECT_CAP_REMOVED_CHAT = BUILDER
                .comment("Message for command /lockdown effect uncap. Placeholders: %effect%")
                .define("effectCapRemovedChat", "Level cap for effect %effect% has been removed.");
        MSG_EFFECT_CLEARED_CHAT = BUILDER
                .comment("Message for command /lockdown effect clear.")
                .define("effectClearedChat", "Effect lists, caps, and bypasses have been cleared.");
        MSG_BYPASS_ADDED_CHAT = BUILDER
                .comment("Message for command /lockdown effect bypass add. Placeholders: %effect%, %item%")
                .define("bypassAddedChat", "Item %item% can now apply effect %effect%.");
        MSG_BYPASS_REMOVED_CHAT = BUILDER
                .comment("Message for command /lockdown effect bypass remove. Placeholders: %effect%, %item%")
                .define("bypassRemovedChat", "Item %item% can no longer apply effect %effect%.");
        BUILDER.pop();
        BUILDER.pop();
    }

    private static volatile Set<String> prohibitedItemIdsCache = Set.of();
    private static volatile Set<String> blockedItemIdsCache = Set.of();
    private static volatile Set<String> excludeItemIdsCache = Set.of();
    private static volatile Map<String, Integer> itemLevelIdsCache = new HashMap<>();
    private static volatile Map<String, Integer> modLevelIdsCache = new HashMap<>();
    private static volatile int configVersion = 0;

    static final ModConfigSpec SPEC = BUILDER.build();


    private static boolean validateItemName(final Object obj) {
        if (!(obj instanceof String itemName)) return false;
        try {
            return BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean validateEffectName(final Object obj) {
        if (!(obj instanceof String effectName)) return false;
        try {
            return BuiltInRegistries.MOB_EFFECT.containsKey(ResourceLocation.parse(effectName));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean validateEffectCapString(final Object obj) {
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

    private static boolean validateEffectBypassString(final Object obj) {
        return obj instanceof String str && EffectLockerConfig.isValidBypassEntry(str);
    }

    private static boolean validateScoreboardName(final Object obj) {
        return true;
    }

    private static boolean validateItemLevelString(final Object obj) {
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

    private static boolean validateModLevelString(final Object obj) {
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

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != Config.SPEC) return;
        if (event instanceof ModConfigEvent.Unloading) return;

        Config.rebuildProhibitedItemIdsCache();
        Config.rebuildBlockedItemIdsCache();
        Config.rebuildExcludeItemIdsCache();
        Config.rebuildItemLevelIdsCache();
        Config.rebuildModLevelIdsCache();
        EffectLockerConfig.rebuild();
    }
}