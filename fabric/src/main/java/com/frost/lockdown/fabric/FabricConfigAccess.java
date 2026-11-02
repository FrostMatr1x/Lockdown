package com.frost.lockdown.fabric;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.frost.lockdown.config.ConfigAccess;
import com.frost.lockdown.config.Setting;
import com.frost.lockdown.config.SimpleSetting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FabricConfigAccess implements ConfigAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger("lockdown");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private LockdownServerConfig model = new LockdownServerConfig();

    private final Setting<List<String>> prohibitedItemIds = list("itemLocker.prohibitedItemIds",
            () -> model.itemLocker.prohibitedItemIds,
            value -> model.itemLocker.prohibitedItemIds = value);
    private final Setting<List<String>> blockedItemIds = list("itemLocker.blockedItemIds",
            () -> model.itemLocker.blockedItemIds,
            value -> model.itemLocker.blockedItemIds = value);
    private final Setting<List<String>> excludeItemIds = list("itemLocker.excludeItemIds",
            () -> model.itemLocker.excludeItemIds,
            value -> model.itemLocker.excludeItemIds = value);
    private final Setting<List<String>> itemLevels = list("itemLocker.itemLevels",
            () -> model.itemLocker.itemLevels,
            value -> model.itemLocker.itemLevels = value);
    private final Setting<List<String>> modLevels = list("itemLocker.modLevels",
            () -> model.itemLocker.modLevels,
            value -> model.itemLocker.modLevels = value);
    private final Setting<String> scoreboardName = str("itemLocker.scoreboard",
            () -> model.itemLocker.scoreboard,
            value -> model.itemLocker.scoreboard = value);
    private final Setting<String> lockNbtTag = str("itemLocker.lockNbtTag",
            () -> model.itemLocker.lockNbtTag,
            value -> model.itemLocker.lockNbtTag = value);

    private final Setting<String> msgItemProhibited = str("itemLocker.messages.itemProhibitedMessage",
            () -> model.itemLocker.messages.itemProhibitedMessage,
            value -> model.itemLocker.messages.itemProhibitedMessage = value);
    private final Setting<String> msgItemBlocked = str("itemLocker.messages.itemBlockedMessage",
            () -> model.itemLocker.messages.itemBlockedMessage,
            value -> model.itemLocker.messages.itemBlockedMessage = value);
    private final Setting<String> msgItemLevelLocked = str("itemLocker.messages.itemLevelLockedMessage",
            () -> model.itemLocker.messages.itemLevelLockedMessage,
            value -> model.itemLocker.messages.itemLevelLockedMessage = value);
    private final Setting<String> msgItemScoreboardError = str("itemLocker.messages.itemScoreboardErrorMessage",
            () -> model.itemLocker.messages.itemScoreboardErrorMessage,
            value -> model.itemLocker.messages.itemScoreboardErrorMessage = value);
    private final Setting<String> msgItemNbt = str("itemLocker.messages.itemNbtMessage",
            () -> model.itemLocker.messages.itemNbtMessage,
            value -> model.itemLocker.messages.itemNbtMessage = value);

    private final Setting<List<String>> effectBanned = list("effectLocker.bannedEffects",
            () -> model.effectLocker.bannedEffects,
            value -> model.effectLocker.bannedEffects = value);
    private final Setting<List<String>> effectCapped = list("effectLocker.cappedEffects",
            () -> model.effectLocker.cappedEffects,
            value -> model.effectLocker.cappedEffects = value);
    private final Setting<List<String>> effectBypassItems = list("effectLocker.effectBypassItems",
            () -> model.effectLocker.effectBypassItems,
            value -> model.effectLocker.effectBypassItems = value);

    private final Setting<String> msgEffectBlocked = str("effectLocker.messages.effectBlockedMessage",
            () -> model.effectLocker.messages.effectBlockedMessage,
            value -> model.effectLocker.messages.effectBlockedMessage = value);
    private final Setting<String> msgEffectCapped = str("effectLocker.messages.effectCappedMessage",
            () -> model.effectLocker.messages.effectCappedMessage,
            value -> model.effectLocker.messages.effectCappedMessage = value);
    private final Setting<String> msgItemUseBlocked = str("effectLocker.messages.itemUseBlockedMessage",
            () -> model.effectLocker.messages.itemUseBlockedMessage,
            value -> model.effectLocker.messages.itemUseBlockedMessage = value);
    private final Setting<String> msgEffectBannedChat = str("effectLocker.messages.effectBannedChat",
            () -> model.effectLocker.messages.effectBannedChat,
            value -> model.effectLocker.messages.effectBannedChat = value);
    private final Setting<String> msgEffectUnbannedChat = str("effectLocker.messages.effectUnbannedChat",
            () -> model.effectLocker.messages.effectUnbannedChat,
            value -> model.effectLocker.messages.effectUnbannedChat = value);
    private final Setting<String> msgEffectCappedChat = str("effectLocker.messages.effectCappedChat",
            () -> model.effectLocker.messages.effectCappedChat,
            value -> model.effectLocker.messages.effectCappedChat = value);
    private final Setting<String> msgEffectCapRemovedChat = str("effectLocker.messages.effectCapRemovedChat",
            () -> model.effectLocker.messages.effectCapRemovedChat,
            value -> model.effectLocker.messages.effectCapRemovedChat = value);
    private final Setting<String> msgEffectClearedChat = str("effectLocker.messages.effectClearedChat",
            () -> model.effectLocker.messages.effectClearedChat,
            value -> model.effectLocker.messages.effectClearedChat = value);
    private final Setting<String> msgBypassAddedChat = str("effectLocker.messages.bypassAddedChat",
            () -> model.effectLocker.messages.bypassAddedChat,
            value -> model.effectLocker.messages.bypassAddedChat = value);
    private final Setting<String> msgBypassRemovedChat = str("effectLocker.messages.bypassRemovedChat",
            () -> model.effectLocker.messages.bypassRemovedChat,
            value -> model.effectLocker.messages.bypassRemovedChat = value);

    public FabricConfigAccess(Path configDir) {
        this.file = configDir.resolve("lockdown-server.json");
        load();
    }

    private Setting<String> str(String id, java.util.function.Supplier<String> getter, java.util.function.Consumer<String> setter) {
        return new SimpleSetting<>(id, getter, setter, this::save);
    }

    private Setting<List<String>> list(String id, java.util.function.Supplier<List<String>> getter, java.util.function.Consumer<List<String>> setter) {
        return new SimpleSetting<>(id, getter, setter, this::save);
    }

    public synchronized void load() {
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                LockdownServerConfig loaded = GSON.fromJson(reader, LockdownServerConfig.class);
                model = loaded != null ? loaded : new LockdownServerConfig();
                model.normalize();
                return;
            } catch (IOException | RuntimeException e) {
                LOGGER.error("Failed to read {}", file, e);
            }
        }
        model = new LockdownServerConfig();
        save();
    }

    public synchronized void save() {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(model, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to write {}", file, e);
        }
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
