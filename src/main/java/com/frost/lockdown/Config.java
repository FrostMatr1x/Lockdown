package com.frost.lockdown;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    /**
     * Список ID запрещенных предметов (Set<String> в логике, List<String> в конфиге)
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PROHIBITED_ITEM_IDS = BUILDER
            .comment("Список ID предметов, которые запрещено перемещать из хранилищ.")
            .defineListAllowEmpty(
                    "creativeItemIds",
                    List.of(),
                    Config::validateItemName
            );
    private static volatile Set<String> prohibitedItemIdsCache = Set.of();
        
    /**
     *  Список заперщенных на уравне предметов. В конфиге это список строк вида "modid:item=level"
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_LEVELS_RAW = BUILDER
            .comment("Список предметов и их требуемых уровней в формате 'modid:item_id=level'. Пример: 'minecraft:diamond=5'")
            .defineListAllowEmpty(
                    "itemLevels",
                    List.of(),
                    Config::validateItemLevelString
            );
    private static volatile Map<String, Integer> itemLevelIdsCache = new HashMap<>();
    
    /**
     *  Список заперщенных на уравне модов. В конфиге это список строк вида "modid=level"
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_LEVELS_RAW = BUILDER
            .comment("Список модов и их требуемых уровней в формате 'modid=level'. Пример: 'minecraft=5'")
            .defineListAllowEmpty(
                    "modLevels",
                    List.of(),
                    Config::validateModLevelString
            );
    private static volatile Map<String, Integer> modLevelIdsCache = new HashMap<>();

    public static final ModConfigSpec.ConfigValue<String> SCOREBOARD_NAME = BUILDER
            .comment("Название Scoreboard")
            .define(
                    "scoreboard",
                     "",
                     Config::validateScoreboardName
            );

    /**
     * Список ID предметов, которые блокируются без уничтожения и без JSON-лога.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_ITEM_IDS = BUILDER
            .comment("Список ID предметов, которые блокируются без уничтожения и без JSON-лога.")
            .defineListAllowEmpty(
                    "blockedItemIds",
                    List.of(),
                    Config::validateItemName
            );
    private static volatile Set<String> blockedItemIdsCache = Set.of();

    /**
     * Имя NBT-тега в custom_data, блокирующего предмет до удаления тега.
     */
    public static final ModConfigSpec.ConfigValue<String> LOCK_NBT_TAG = BUILDER
            .comment("Имя NBT-тега в custom_data, который блокирует предмет до удаления тега.")
            .define(
                    "lockNbtTag",
                    "itemlocker:locked"
            );

    private static volatile int configVersion = 0;

    /**
     * Список ID исключений в моде предметов (Set<String> в логике, List<String> в конфиге)
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDE_ITEM_IDS = BUILDER
            .comment("Список исключенных ID предметов, которые разрешины на любом level.")
            .defineListAllowEmpty(
                    "excludeItemIds",
                    List.of(),
                    Config::validateItemName
            );
    private static volatile Set<String> excludeItemIdsCache = Set.of();

    /**
     * Список ID исключений в моде предметов (Set<String> в логике, List<String> в конфиге)
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> STORAGE_IDS = BUILDER
            .comment("Список исключенных ID предметов, которые разрешины на любом level.")
            .defineListAllowEmpty(
                    "excludeItemIds",
                    List.of(),
                    Config::validateItemName
            );
    private static volatile Set<String> storageIdsCache = Set.of();

    static final ModConfigSpec SPEC = BUILDER.build();

    // ВАЛИДАТОРЫ

    // Проверка, что строка является валидным ID предмета
    private static boolean validateItemName(final Object obj) {
        if (!(obj instanceof String itemName)) return false;
        try {
            return BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean validateScoreboardName(final Object obj) {
        return true;
    }

    // Проверка, что строка имеет формат "item_id=level" и item_id существует
    private static boolean validateItemLevelString(final Object obj) {
        if (!(obj instanceof String str)) return false;
        
        String[] parts = str.split("=");
        if (parts.length != 2) return false;
        
        String itemId = parts[0].trim();
        String levelStr = parts[1].trim();
        
        if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemId))) {
            return false;
        }
        
        try {
            Integer.parseInt(levelStr);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Проверка, что строка имеет формат "item_id=level" и item_id существует
    private static boolean validateModLevelString(final Object obj) {
        if (!(obj instanceof String str)) return false;
        
        String[] parts = str.split("=");
        if (parts.length != 2) return false;
    
        String levelStr = parts[1].trim();
        
        try {
            Integer.parseInt(levelStr);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // УДОБНЫЕ МЕТОДЫ ДЛЯ ПОЛУЧЕНИЯ ДАННЫХ В КОДЕ 

    /**
     * Возвращает Set<String> с ID полностью запрещённых предметов для быстрой проверки.
     */
    public static Set<String> getProhibitedItemIdsSet() {
        return prohibitedItemIdsCache;
    }

    /**
     * Возвращает Set<String> с ID запрещенных на левеле предметов для быстрой проверки.
     */
    public static Map<String, Integer> getItemLevelIdsMap() {
        return itemLevelIdsCache;
    }

    /**
     * Возвращает Set<String> с ID запрещенных на левеле предметов для быстрой проверки.
     */
    public static Map<String, Integer> getModLevelIdsMap() {
        return modLevelIdsCache;
    }

    /**
     * Возвращает Set<String> с ID исключений предметов для быстрой проверки.
     */
    public static Set<String> getModExcludeIdsSet() {
        return excludeItemIdsCache;
    }

    /**
     * Возвращает Set<String> с ID блокируемых предметов для быстрой проверки.
     */
    public static Set<String> getBlockedItemIdsSet() {
        return blockedItemIdsCache;
    }

    /**
     * Возвращает имя NBT-тега блокировки.
     */
    public static String getLockNbtTag() {
        return LOCK_NBT_TAG.get();
    }

    /**
     * Возвращает текущую версию конфигурации (инкрементируется при каждой пересборке кэша).
     */
    public static int getConfigVersion() {
        return configVersion;
    }

    /**
     * Пересобирает кэш. Вызывать при загрузке конфига и после любого .set()/.save().
     */
    public static void rebuildProhibitedItemIdsCache() {
        prohibitedItemIdsCache = new HashSet<>(PROHIBITED_ITEM_IDS.get());
        configVersion++;
    }

    /**
     * Пересобирает кэш. Вызывать при загрузке конфига и после любого .set()/.save().
     */
    public static void rebuildExcludeItemIdsCache() {
        excludeItemIdsCache = new HashSet<>(EXCLUDE_ITEM_IDS.get());
        configVersion++;
    }

    /**
     * Пересобирает кэш. Вызывать при загрузке конфига и после любого .set()/.save().
     */
    public static void rebuildItemLevelIdsCache() {
        itemLevelIdsCache.clear();

        for (var itemAndLevel : ITEM_LEVELS_RAW.get()) {
            String[] split = itemAndLevel.split("=");
            itemLevelIdsCache.put(split[0], Integer.parseInt(split[1]));
        }
        configVersion++;
    }

    /**
     * Пересобирает кэш. Вызывать при загрузке конфига и после любого .set()/.save().
     */
    public static void rebuildModLevelIdsCache() {
        modLevelIdsCache.clear();

        for (var modAndLevel : MOD_LEVELS_RAW.get()) {
            String[] split = modAndLevel.split("=");
            modLevelIdsCache.put(split[0], Integer.parseInt(split[1]));
        }
        configVersion++;
    }

    /**
     * Пересобирает кэш блокируемых предметов. Вызывать при загрузке конфига и после любого .set()/.save().
     */
    public static void rebuildBlockedItemIdsCache() {
        blockedItemIdsCache = new HashSet<>(BLOCKED_ITEM_IDS.get());
        configVersion++;
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != Config.SPEC) return;
        if (event instanceof ModConfigEvent.Unloading) return;

        Config.rebuildProhibitedItemIdsCache();
        Config.rebuildExcludeItemIdsCache();
        Config.rebuildItemLevelIdsCache();
        Config.rebuildModLevelIdsCache();
        Config.rebuildBlockedItemIdsCache();
    }
}