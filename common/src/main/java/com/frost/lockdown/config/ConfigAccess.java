package com.frost.lockdown.config;

import java.util.List;

public interface ConfigAccess {

    final class Holder {
        private static volatile ConfigAccess current = new DefaultConfigAccess();

        private Holder() {
        }
    }

    static ConfigAccess get() {
        return Holder.current;
    }

    static void set(ConfigAccess access) {
        Holder.current = access != null ? access : new DefaultConfigAccess();
    }

    Setting<List<String>> prohibitedItemIds();

    Setting<List<String>> blockedItemIds();

    Setting<List<String>> excludeItemIds();

    Setting<List<String>> itemLevels();

    Setting<List<String>> modLevels();

    Setting<String> scoreboardName();

    Setting<String> lockNbtTag();

    Setting<String> msgItemProhibited();

    Setting<String> msgItemBlocked();

    Setting<String> msgItemLevelLocked();

    Setting<String> msgItemScoreboardError();

    Setting<String> msgItemNbt();

    Setting<List<String>> effectBanned();

    Setting<List<String>> effectCapped();

    Setting<List<String>> effectBypassItems();

    Setting<String> msgEffectBlocked();

    Setting<String> msgEffectCapped();

    Setting<String> msgItemUseBlocked();

    Setting<String> msgEffectBannedChat();

    Setting<String> msgEffectUnbannedChat();

    Setting<String> msgEffectCappedChat();

    Setting<String> msgEffectCapRemovedChat();

    Setting<String> msgEffectClearedChat();

    Setting<String> msgBypassAddedChat();

    Setting<String> msgBypassRemovedChat();
}
