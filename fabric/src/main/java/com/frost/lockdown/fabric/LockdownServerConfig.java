package com.frost.lockdown.fabric;

import java.util.ArrayList;
import java.util.List;

public final class LockdownServerConfig {

    public ItemLocker itemLocker = new ItemLocker();
    public EffectLocker effectLocker = new EffectLocker();

    public void normalize() {
        if (itemLocker == null) {
            itemLocker = new ItemLocker();
        }
        itemLocker.normalize();

        if (effectLocker == null) {
            effectLocker = new EffectLocker();
        }
        effectLocker.normalize();
    }

    public static final class ItemLocker {
        public List<String> prohibitedItemIds = new ArrayList<>();
        public List<String> blockedItemIds = new ArrayList<>();
        public List<String> excludeItemIds = new ArrayList<>();
        public List<String> itemLevels = new ArrayList<>();
        public List<String> modLevels = new ArrayList<>();
        public String scoreboard = "";
        public String lockNbtTag = "lockdown:locked";
        public Messages messages = new Messages();

        private void normalize() {
            if (prohibitedItemIds == null) prohibitedItemIds = new ArrayList<>();
            if (blockedItemIds == null) blockedItemIds = new ArrayList<>();
            if (excludeItemIds == null) excludeItemIds = new ArrayList<>();
            if (itemLevels == null) itemLevels = new ArrayList<>();
            if (modLevels == null) modLevels = new ArrayList<>();
            if (scoreboard == null) scoreboard = "";
            if (lockNbtTag == null) lockNbtTag = "lockdown:locked";
            if (messages == null) messages = new Messages();
            messages.normalize();
        }

        public static final class Messages {
            public String itemProhibitedMessage = "Item %item% is prohibited on the server and was destroyed.";
            public String itemBlockedMessage = "Item %item% is blocked on the server.";
            public String itemLevelLockedMessage = "Item %item% is locked. Required level: %level%, your level: %score%.";
            public String itemScoreboardErrorMessage = "You are missing the required scoreboard. Please contact server administration.";
            public String itemNbtMessage = "The lock tag has been removed from item %item%.";

            private void normalize() {
                if (itemProhibitedMessage == null) itemProhibitedMessage = "Item %item% is prohibited on the server and was destroyed.";
                if (itemBlockedMessage == null) itemBlockedMessage = "Item %item% is blocked on the server.";
                if (itemLevelLockedMessage == null) itemLevelLockedMessage = "Item %item% is locked. Required level: %level%, your level: %score%.";
                if (itemScoreboardErrorMessage == null) itemScoreboardErrorMessage = "You are missing the required scoreboard. Please contact server administration.";
                if (itemNbtMessage == null) itemNbtMessage = "The lock tag has been removed from item %item%.";
            }
        }
    }

    public static final class EffectLocker {
        public List<String> bannedEffects = new ArrayList<>();
        public List<String> cappedEffects = new ArrayList<>();
        public List<String> effectBypassItems = new ArrayList<>();
        public Messages messages = new Messages();

        private void normalize() {
            if (bannedEffects == null) bannedEffects = new ArrayList<>();
            if (cappedEffects == null) cappedEffects = new ArrayList<>();
            if (effectBypassItems == null) effectBypassItems = new ArrayList<>();
            if (messages == null) messages = new Messages();
            messages.normalize();
        }

        public static final class Messages {
            public String effectBlockedMessage = "Effect %effect% is prohibited on the server.";
            public String effectCappedMessage = "Effect %effect% is capped at level %level%.";
            public String itemUseBlockedMessage = "Item %item% cannot apply effect %effect%.";
            public String effectBannedChat = "Effect %effect% has been banned and removed from %count% players.";
            public String effectUnbannedChat = "Effect %effect% has been unbanned.";
            public String effectCappedChat = "Max level for effect %effect% is set to %level%.";
            public String effectCapRemovedChat = "Level cap for effect %effect% has been removed.";
            public String effectClearedChat = "Effect lists, caps, and bypasses have been cleared.";
            public String bypassAddedChat = "Item %item% can now apply effect %effect%.";
            public String bypassRemovedChat = "Item %item% can no longer apply effect %effect%.";

            private void normalize() {
                if (effectBlockedMessage == null) effectBlockedMessage = "Effect %effect% is prohibited on the server.";
                if (effectCappedMessage == null) effectCappedMessage = "Effect %effect% is capped at level %level%.";
                if (itemUseBlockedMessage == null) itemUseBlockedMessage = "Item %item% cannot apply effect %effect%.";
                if (effectBannedChat == null) effectBannedChat = "Effect %effect% has been banned and removed from %count% players.";
                if (effectUnbannedChat == null) effectUnbannedChat = "Effect %effect% has been unbanned.";
                if (effectCappedChat == null) effectCappedChat = "Max level for effect %effect% is set to %level%.";
                if (effectCapRemovedChat == null) effectCapRemovedChat = "Level cap for effect %effect% has been removed.";
                if (effectClearedChat == null) effectClearedChat = "Effect lists, caps, and bypasses have been cleared.";
                if (bypassAddedChat == null) bypassAddedChat = "Item %item% can now apply effect %effect%.";
                if (bypassRemovedChat == null) bypassRemovedChat = "Item %item% can no longer apply effect %effect%.";
            }
        }
    }
}
