package com.frost.lockdown.config;

import java.util.ArrayList;
import java.util.List;

public final class DefaultConfigAccess implements ConfigAccess {

    private final Setting<List<String>> prohibitedItemIds = new DefaultSetting<>("itemLocker.prohibitedItemIds", new ArrayList<>());
    private final Setting<List<String>> blockedItemIds = new DefaultSetting<>("itemLocker.blockedItemIds", new ArrayList<>());
    private final Setting<List<String>> excludeItemIds = new DefaultSetting<>("itemLocker.excludeItemIds", new ArrayList<>());
    private final Setting<List<String>> itemLevels = new DefaultSetting<>("itemLocker.itemLevels", new ArrayList<>());
    private final Setting<List<String>> modLevels = new DefaultSetting<>("itemLocker.modLevels", new ArrayList<>());
    private final Setting<String> scoreboardName = new DefaultSetting<>("itemLocker.scoreboard", "");
    private final Setting<String> lockNbtTag = new DefaultSetting<>("itemLocker.lockNbtTag", "lockdown:locked");

    private final Setting<String> msgItemProhibited = new DefaultSetting<>("itemLocker.messages.itemProhibitedMessage",
            "Item %item% is prohibited on the server and was destroyed.");
    private final Setting<String> msgItemBlocked = new DefaultSetting<>("itemLocker.messages.itemBlockedMessage",
            "Item %item% is blocked on the server.");
    private final Setting<String> msgItemLevelLocked = new DefaultSetting<>("itemLocker.messages.itemLevelLockedMessage",
            "Item %item% is locked. Required level: %level%, your level: %score%.");
    private final Setting<String> msgItemScoreboardError = new DefaultSetting<>("itemLocker.messages.itemScoreboardErrorMessage",
            "You are missing the required scoreboard. Please contact server administration.");
    private final Setting<String> msgItemNbt = new DefaultSetting<>("itemLocker.messages.itemNbtMessage",
            "The lock tag has been removed from item %item%.");

    private final Setting<List<String>> effectBanned = new DefaultSetting<>("effectLocker.bannedEffects", new ArrayList<>());
    private final Setting<List<String>> effectCapped = new DefaultSetting<>("effectLocker.cappedEffects", new ArrayList<>());
    private final Setting<List<String>> effectBypassItems = new DefaultSetting<>("effectLocker.effectBypassItems", new ArrayList<>());

    private final Setting<String> msgEffectBlocked = new DefaultSetting<>("effectLocker.messages.effectBlockedMessage",
            "Effect %effect% is prohibited on the server.");
    private final Setting<String> msgEffectCapped = new DefaultSetting<>("effectLocker.messages.effectCappedMessage",
            "Effect %effect% is capped at level %level%.");
    private final Setting<String> msgItemUseBlocked = new DefaultSetting<>("effectLocker.messages.itemUseBlockedMessage",
            "Item %item% cannot apply effect %effect%.");
    private final Setting<String> msgEffectBannedChat = new DefaultSetting<>("effectLocker.messages.effectBannedChat",
            "Effect %effect% has been banned and removed from %count% players.");
    private final Setting<String> msgEffectUnbannedChat = new DefaultSetting<>("effectLocker.messages.effectUnbannedChat",
            "Effect %effect% has been unbanned.");
    private final Setting<String> msgEffectCappedChat = new DefaultSetting<>("effectLocker.messages.effectCappedChat",
            "Max level for effect %effect% is set to %level%.");
    private final Setting<String> msgEffectCapRemovedChat = new DefaultSetting<>("effectLocker.messages.effectCapRemovedChat",
            "Level cap for effect %effect% has been removed.");
    private final Setting<String> msgEffectClearedChat = new DefaultSetting<>("effectLocker.messages.effectClearedChat",
            "Effect lists, caps, and bypasses have been cleared.");
    private final Setting<String> msgBypassAddedChat = new DefaultSetting<>("effectLocker.messages.bypassAddedChat",
            "Item %item% can now apply effect %effect%.");
    private final Setting<String> msgBypassRemovedChat = new DefaultSetting<>("effectLocker.messages.bypassRemovedChat",
            "Item %item% can no longer apply effect %effect%.");

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
