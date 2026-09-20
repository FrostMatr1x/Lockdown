# 🔒 Lockdown

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen?style=for-the-badge&logo=minecraft" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/Loader-NeoForge-orange?style=for-the-badge" alt="NeoForge">
  <img src="https://img.shields.io/badge/Environment-Server-blue?style=for-the-badge" alt="Server-Side">
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License">
</p>

**Lockdown** is a flexible, server-side gameplay management tool for Minecraft (NeoForge 1.21.1). The mod merges advanced item restrictions (scoreboard-based levels, NBT locks, bans) and granular status effect control.

Ideal for **RPG modpacks**, progression/staging-based servers, and fine-grained gameplay balance.

---

## ✨ Features

### 📦 1. ItemLocker (Item Control)
* **Blacklist (Prohibited):** Completely destroys prohibited items whenever they enter a player's inventory (violators are logged to `itemLockerBan.json`).
* **Usage Restriction (Blocked):** Disallows using or equipping specific items without deleting them.
* **Progression System (Scoreboard Levels):** Gates items and equipment behind a player's scoreboard score (supports per-item rules as well as mod-wide rules with exclusion lists).
* **NBT Locking:** Restricts specific item instances based on a custom NBT tag.

### 🧪 2. EffectLocker (Status Effect Control)
* **Banning Effects (Ban):** Completely prevents selected status effects from being applied.
* **Amplifier Capping (Cap):** Limits maximum effect potency (amplifier levels `0` to `255`, e.g., capping Strength II down to Strength I).
* **Source Whitelisting (Bypass):** Allows banned/restricted effects to be applied only when originating from specific items (such as dedicated potions or artifacts).
* **Graceful Handling:** Item consumption (drinking, eating) is never canceled — animations and resource consumption happen as expected; only the status effect itself is suppressed.

---

## 📥 Installation

1. Download the latest `.jar` from [Releases](../../releases) or build it locally (`./gradlew build`).
2. Drop `lockdown-<version>.jar` into your server's `mods/` directory.
3. Launch the server once to generate the default configuration file: `config/lockdown-server.toml`.

---

## 🛠️ Commands

> [!IMPORTANT]
> All commands require Operator Permission Level 4 (`OP level 4`).

### Item Management (`/lockdown item`)

| Command | Description |
| :--- | :--- |
| `/lockdown item prohibitedList addFromHand\|removeFromHand\|list` | Manage the list of completely destroyed items |
| `/lockdown item blockedList addFromHand\|removeFromHand\|list` | Manage blocked items (restricted from use, but not destroyed) |
| `/lockdown item level item addFromHand <lvl>\|removeFromHand\|list` | Set the required scoreboard level for the held item |
| `/lockdown item level mod addFromHand <lvl>\|removeFromHand\|list` | Set the required level for all items from the held item's mod |
| `/lockdown item level mod addExcludeFromHand\|removeExcludeFromHand\|listExclude` | Manage exclusions from mod-wide level restrictions |

### Effect Management (`/lockdown effect`)

| Command | Description |
| :--- | :--- |
| `/lockdown effect ban <effect>` | Ban an effect from being applied |
| `/lockdown effect unban <effect>` | Unban a previously restricted effect |
| `/lockdown effect cap <effect> <level>` | Cap maximum effect amplifier (0–255) |
| `/lockdown effect uncap <effect>` | Remove an amplifier cap |
| `/lockdown effect bypass add <effect> <item>` | Allow a specific item to bypass effect restrictions |
| `/lockdown effect bypass remove <effect> <item>` | Remove an item from the bypass list |
| `/lockdown effect list` | View active effect restrictions and rules |
| `/lockdown effect clear` | Reset all effect configurations |

### General Settings

| Command | Description |
| :--- | :--- |
| `/lockdown scoreboard set <name>` / `get` | Set / view the scoreboard objective used for level progression |
| `/lockdown lockNBT set <tag>` / `get` | Set / view the NBT tag used for locked items |

---

## ⚙️ Configuration

The config file is located at `config/lockdown-server.toml` and split into `itemLocker` and `effectLocker` sections.

### Message Placeholders
You can customize chat notifications using the following placeholders:
* `%item%` — Registry ID of the item (`minecraft:diamond_sword`).
* `%player%` — Player username.
* `%effect%` — Registry ID of the status effect (`minecraft:strength`).
* `%level%` — Required or maximum allowed level.
* `%score%` — Player's current scoreboard score.
* `%count%` — Amount of confiscated/destroyed items.

### Effect Bypass Format
Bypass rules follow the pattern: `modid:effect|modid:item1;modid:item2`.

```toml
[effectLocker]
effectBypassItems = [
    # Allows Weakness effect only when applied via regular or lingering potions
    "minecraft:weakness|minecraft:potion;minecraft:lingering_potion"
]
```

---

## 🎒 Sophisticated Backpacks Compatibility

> [!NOTE]
> Integration with **Sophisticated Backpacks** is completely **optional**.

* If the mod is installed on the server, Lockdown will automatically inspect backpack inventories for prohibited items.
* If the mod is absent, Lockdown functions normally with zero missing dependency errors.

---

## 📌 Technical Details & Quirks

* **EffectLocker:** Item interaction (drinking potions, eating food) is **never interrupted** — the item is consumed, and sounds/animations play normally. Only the application of the status effect to the player is intercepted.
* **Registry Identification:** All checks are resolved strictly against valid `ResourceLocation`s (`namespace:path`).
* **Cheat Logging:** The `itemLockerBan.json` file is only populated by `ItemLocker` when unauthorized prohibited items are confiscated. The effect module does not log entries there.
