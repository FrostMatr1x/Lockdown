package com.frost.lockdown.neoforge;

import java.util.ArrayList;
import java.util.List;

import com.frost.lockdown.Config;
import com.frost.lockdown.config.ConfigAccess;
import com.frost.lockdown.config.Setting;
import com.frost.lockdown.config.SimpleSetting;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NeoForgeConfigAccess implements ConfigAccess {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> PROHIBITED_ITEM_IDS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_ITEM_IDS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDE_ITEM_IDS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_LEVELS_RAW;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_LEVELS_RAW;
    private static final ModConfigSpec.ConfigValue<String> SCOREBOARD_NAME;
    private static final ModConfigSpec.ConfigValue<String> LOCK_NBT_TAG;

    private static final ModConfigSpec.ConfigValue<String> MSG_ITEM_PROHIBITED;
    private static final ModConfigSpec.ConfigValue<String> MSG_ITEM_BLOCKED;
    private static final ModConfigSpec.ConfigValue<String> MSG_ITEM_LEVEL_LOCKED;
    private static final ModConfigSpec.ConfigValue<String> MSG_ITEM_SCOREBOARD_ERROR;
    private static final ModConfigSpec.ConfigValue<String> MSG_ITEM_NBT;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> EFFECT_BANNED;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> EFFECT_CAPPED;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> EFFECT_BYPASS_ITEMS;

    private static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_BLOCKED;
    private static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_CAPPED;
    private static final ModConfigSpec.ConfigValue<String> MSG_ITEM_USE_BLOCKED;
    private static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_BANNED_CHAT;
    private static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_UNBANNED_CHAT;
    private static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_CAPPED_CHAT;
    private static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_CAP_REMOVED_CHAT;
    private static final ModConfigSpec.ConfigValue<String> MSG_EFFECT_CLEARED_CHAT;
    private static final ModConfigSpec.ConfigValue<String> MSG_BYPASS_ADDED_CHAT;
    private static final ModConfigSpec.ConfigValue<String> MSG_BYPASS_REMOVED_CHAT;

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

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static final NeoForgeConfigAccess INSTANCE = new NeoForgeConfigAccess();

    private final Setting<List<String>> prohibitedItemIds = list(PROHIBITED_ITEM_IDS, "itemLocker.prohibitedItemIds");
    private final Setting<List<String>> blockedItemIds = list(BLOCKED_ITEM_IDS, "itemLocker.blockedItemIds");
    private final Setting<List<String>> excludeItemIds = list(EXCLUDE_ITEM_IDS, "itemLocker.excludeItemIds");
    private final Setting<List<String>> itemLevels = list(ITEM_LEVELS_RAW, "itemLocker.itemLevels");
    private final Setting<List<String>> modLevels = list(MOD_LEVELS_RAW, "itemLocker.modLevels");
    private final Setting<String> scoreboardName = str(SCOREBOARD_NAME, "itemLocker.scoreboard");
    private final Setting<String> lockNbtTag = str(LOCK_NBT_TAG, "itemLocker.lockNbtTag");

    private final Setting<String> msgItemProhibited = str(MSG_ITEM_PROHIBITED, "itemLocker.messages.itemProhibitedMessage");
    private final Setting<String> msgItemBlocked = str(MSG_ITEM_BLOCKED, "itemLocker.messages.itemBlockedMessage");
    private final Setting<String> msgItemLevelLocked = str(MSG_ITEM_LEVEL_LOCKED, "itemLocker.messages.itemLevelLockedMessage");
    private final Setting<String> msgItemScoreboardError = str(MSG_ITEM_SCOREBOARD_ERROR, "itemLocker.messages.itemScoreboardErrorMessage");
    private final Setting<String> msgItemNbt = str(MSG_ITEM_NBT, "itemLocker.messages.itemNbtMessage");

    private final Setting<List<String>> effectBanned = list(EFFECT_BANNED, "effectLocker.bannedEffects");
    private final Setting<List<String>> effectCapped = list(EFFECT_CAPPED, "effectLocker.cappedEffects");
    private final Setting<List<String>> effectBypassItems = list(EFFECT_BYPASS_ITEMS, "effectLocker.effectBypassItems");

    private final Setting<String> msgEffectBlocked = str(MSG_EFFECT_BLOCKED, "effectLocker.messages.effectBlockedMessage");
    private final Setting<String> msgEffectCapped = str(MSG_EFFECT_CAPPED, "effectLocker.messages.effectCappedMessage");
    private final Setting<String> msgItemUseBlocked = str(MSG_ITEM_USE_BLOCKED, "effectLocker.messages.itemUseBlockedMessage");
    private final Setting<String> msgEffectBannedChat = str(MSG_EFFECT_BANNED_CHAT, "effectLocker.messages.effectBannedChat");
    private final Setting<String> msgEffectUnbannedChat = str(MSG_EFFECT_UNBANNED_CHAT, "effectLocker.messages.effectUnbannedChat");
    private final Setting<String> msgEffectCappedChat = str(MSG_EFFECT_CAPPED_CHAT, "effectLocker.messages.effectCappedChat");
    private final Setting<String> msgEffectCapRemovedChat = str(MSG_EFFECT_CAP_REMOVED_CHAT, "effectLocker.messages.effectCapRemovedChat");
    private final Setting<String> msgEffectClearedChat = str(MSG_EFFECT_CLEARED_CHAT, "effectLocker.messages.effectClearedChat");
    private final Setting<String> msgBypassAddedChat = str(MSG_BYPASS_ADDED_CHAT, "effectLocker.messages.bypassAddedChat");
    private final Setting<String> msgBypassRemovedChat = str(MSG_BYPASS_REMOVED_CHAT, "effectLocker.messages.bypassRemovedChat");

    private NeoForgeConfigAccess() {
    }

    private static Setting<String> str(ModConfigSpec.ConfigValue<String> value, String id) {
        return new SimpleSetting<>(id, value::get, value::set, value::save);
    }

    private static Setting<List<String>> list(ModConfigSpec.ConfigValue<List<? extends String>> value, String id) {
        return new SimpleSetting<>(id,
                () -> new ArrayList<>(value.get()),
                value::set,
                value::save);
    }

    @Override
    public Setting<List<String>> prohibitedItemIds() {
        return prohibitedItemIds;
    }

    @Override
    public Setting<List<String>> blockedItemIds() {
        return blockedItemIds;
    }

    @Override
    public Setting<List<String>> excludeItemIds() {
        return excludeItemIds;
    }

    @Override
    public Setting<List<String>> itemLevels() {
        return itemLevels;
    }

    @Override
    public Setting<List<String>> modLevels() {
        return modLevels;
    }

    @Override
    public Setting<String> scoreboardName() {
        return scoreboardName;
    }

    @Override
    public Setting<String> lockNbtTag() {
        return lockNbtTag;
    }

    @Override
    public Setting<String> msgItemProhibited() {
        return msgItemProhibited;
    }

    @Override
    public Setting<String> msgItemBlocked() {
        return msgItemBlocked;
    }

    @Override
    public Setting<String> msgItemLevelLocked() {
        return msgItemLevelLocked;
    }

    @Override
    public Setting<String> msgItemScoreboardError() {
        return msgItemScoreboardError;
    }

    @Override
    public Setting<String> msgItemNbt() {
        return msgItemNbt;
    }

    @Override
    public Setting<List<String>> effectBanned() {
        return effectBanned;
    }

    @Override
    public Setting<List<String>> effectCapped() {
        return effectCapped;
    }

    @Override
    public Setting<List<String>> effectBypassItems() {
        return effectBypassItems;
    }

    @Override
    public Setting<String> msgEffectBlocked() {
        return msgEffectBlocked;
    }

    @Override
    public Setting<String> msgEffectCapped() {
        return msgEffectCapped;
    }

    @Override
    public Setting<String> msgItemUseBlocked() {
        return msgItemUseBlocked;
    }

    @Override
    public Setting<String> msgEffectBannedChat() {
        return msgEffectBannedChat;
    }

    @Override
    public Setting<String> msgEffectUnbannedChat() {
        return msgEffectUnbannedChat;
    }

    @Override
    public Setting<String> msgEffectCappedChat() {
        return msgEffectCappedChat;
    }

    @Override
    public Setting<String> msgEffectCapRemovedChat() {
        return msgEffectCapRemovedChat;
    }

    @Override
    public Setting<String> msgEffectClearedChat() {
        return msgEffectClearedChat;
    }

    @Override
    public Setting<String> msgBypassAddedChat() {
        return msgBypassAddedChat;
    }

    @Override
    public Setting<String> msgBypassRemovedChat() {
        return msgBypassRemovedChat;
    }
}
