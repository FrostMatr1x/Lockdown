package com.frost.lockdown.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.frost.lockdown.Config;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public final class EffectLockerConfig {

    private static final Set<String> BANNED_CACHE = new HashSet<>();
    private static final Map<String, Integer> CAP_CACHE = new HashMap<>();
    private static final Map<String, Set<String>> BYPASS_CACHE = new HashMap<>();

    private EffectLockerConfig() {
    }

    public static void rebuild() {
        BANNED_CACHE.clear();
        for (String entry : Config.EFFECT_BANNED.get()) {
            ResourceLocation id = parseId(entry);
            if (id != null) {
                BANNED_CACHE.add(id.toString());
            }
        }

        CAP_CACHE.clear();
        for (String entry : Config.EFFECT_CAPPED.get()) {
            Cap cap = parseCap(entry);
            if (cap != null) {
                CAP_CACHE.put(cap.id(), cap.amplifier());
            }
        }

        BYPASS_CACHE.clear();
        for (String entry : Config.EFFECT_BYPASS_ITEMS.get()) {
            BypassEntry parsed = parseBypass(entry);
            if (parsed != null) {
                BYPASS_CACHE.computeIfAbsent(parsed.effectId(), k -> new HashSet<>()).addAll(parsed.itemIds());
            }
        }
    }

    // ===================== BANNED =====================

    public static boolean isBanned(Holder<MobEffect> effect) {
        return resolveId(effect) != null && isBanned(resolveId(effect));
    }

    public static boolean isBanned(ResourceLocation id) {
        return id != null && isBanned(id.toString());
    }

    public static boolean isBanned(String id) {
        return id != null && BANNED_CACHE.contains(normalize(id));
    }

    public static List<String> getBannedEffects() {
        return new ArrayList<>(Config.EFFECT_BANNED.get());
    }

    public static boolean addBanned(String id) {
        if (isBanned(id)) {
            return false;
        }
        List<String> list = new ArrayList<>(Config.EFFECT_BANNED.get());
        list.add(id);
        Config.EFFECT_BANNED.set(list);
        Config.EFFECT_BANNED.save();
        rebuild();
        return true;
    }

    public static boolean removeBanned(String id) {
        List<String> list = new ArrayList<>(Config.EFFECT_BANNED.get());
        boolean removed = list.removeIf(entry -> id.equalsIgnoreCase(entry.trim()));
        if (removed) {
            Config.EFFECT_BANNED.set(list);
            Config.EFFECT_BANNED.save();
            rebuild();
        }
        return removed;
    }

    // ===================== CAP =====================

    public static int getMaxAmplifier(Holder<MobEffect> effect) {
        String id = resolveId(effect);
        return id != null ? getMaxAmplifier(id) : -1;
    }

    public static int getMaxAmplifier(ResourceLocation id) {
        return id != null ? getMaxAmplifier(id.toString()) : -1;
    }

    public static int getMaxAmplifier(String id) {
        if (id == null) {
            return -1;
        }
        Integer cap = CAP_CACHE.get(normalize(id));
        return cap != null ? cap : -1;
    }

    public static List<String> getCappedEffects() {
        return new ArrayList<>(Config.EFFECT_CAPPED.get());
    }

    public static boolean setCap(String id, int amplifier) {
        List<String> list = new ArrayList<>(Config.EFFECT_CAPPED.get());
        list.removeIf(entry -> {
            Cap cap = parseCap(entry);
            return cap != null && id.equalsIgnoreCase(cap.id());
        });
        list.add(id + "=" + amplifier);
        Config.EFFECT_CAPPED.set(list);
        Config.EFFECT_CAPPED.save();
        rebuild();
        return true;
    }

    public static boolean removeCap(String id) {
        List<String> list = new ArrayList<>(Config.EFFECT_CAPPED.get());
        boolean removed = list.removeIf(entry -> {
            Cap cap = parseCap(entry);
            return cap != null && id.equalsIgnoreCase(cap.id());
        });
        if (removed) {
            Config.EFFECT_CAPPED.set(list);
            Config.EFFECT_CAPPED.save();
            rebuild();
        }
        return removed;
    }

    // ===================== BYPASS =====================

    public static boolean isBypassed(Holder<MobEffect> effect, String itemId) {
        String effectId = resolveId(effect);
        return effectId != null && isBypassed(effectId, itemId);
    }

    public static boolean isBypassed(String effectId, String itemId) {
        if (effectId == null || itemId == null) {
            return false;
        }
        Set<String> items = BYPASS_CACHE.get(normalize(effectId));
        return items != null && items.contains(normalize(itemId));
    }

    public static Map<String, Set<String>> getBypassMap() {
        return BYPASS_CACHE;
    }

    public static boolean addBypass(String effectId, String itemId) {
        if (isBypassed(effectId, itemId)) {
            return false;
        }
        List<String> list = new ArrayList<>(Config.EFFECT_BYPASS_ITEMS.get());
        int index = -1;
        BypassEntry target = null;
        for (int i = 0; i < list.size(); i++) {
            BypassEntry parsed = parseBypass(list.get(i));
            if (parsed != null && effectId.equalsIgnoreCase(parsed.effectId())) {
                index = i;
                target = parsed;
                break;
            }
        }

        if (target == null) {
            list.add(effectId + "|" + itemId);
        } else {
            Set<String> merged = new HashSet<>(target.itemIds());
            merged.add(itemId);
            list.set(index, effectId + "|" + String.join(";", merged));
        }

        Config.EFFECT_BYPASS_ITEMS.set(list);
        Config.EFFECT_BYPASS_ITEMS.save();
        rebuild();
        return true;
    }

    public static boolean removeBypass(String effectId, String itemId) {
        List<String> list = new ArrayList<>(Config.EFFECT_BYPASS_ITEMS.get());
        boolean removed = false;
        for (int i = 0; i < list.size(); i++) {
            BypassEntry parsed = parseBypass(list.get(i));
            if (parsed == null || !effectId.equalsIgnoreCase(parsed.effectId())) {
                continue;
            }
            Set<String> items = new HashSet<>(parsed.itemIds());
            if (items.removeIf(entry -> entry.equalsIgnoreCase(itemId))) {
                removed = true;
                if (items.isEmpty()) {
                    list.remove(i);
                } else {
                    list.set(i, effectId + "|" + String.join(";", items));
                }
                break;
            }
        }
        if (removed) {
            Config.EFFECT_BYPASS_ITEMS.set(list);
            Config.EFFECT_BYPASS_ITEMS.save();
            rebuild();
        }
        return removed;
    }

    // ===================== CLEAR =====================

    public static void clearAll() {
        Config.EFFECT_BANNED.set(new ArrayList<>());
        Config.EFFECT_BANNED.save();
        Config.EFFECT_CAPPED.set(new ArrayList<>());
        Config.EFFECT_CAPPED.save();
        Config.EFFECT_BYPASS_ITEMS.set(new ArrayList<>());
        Config.EFFECT_BYPASS_ITEMS.save();
        rebuild();
    }

    // ===================== PARSING =====================

    public static boolean isValidBypassEntry(String entry) {
        return parseBypass(entry) != null;
    }

    private static String resolveId(Holder<MobEffect> effect) {
        if (effect == null) {
            return null;
        }
        return effect.unwrapKey().map(ResourceKey::location).map(ResourceLocation::toString).orElse(null);
    }

    private static String normalize(String id) {
        ResourceLocation parsed = parseId(id);
        return parsed != null ? parsed.toString() : id.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static ResourceLocation parseId(String value) {
        if (value == null) {
            return null;
        }
        return ResourceLocation.tryParse(value.trim());
    }

    private static Cap parseCap(String entry) {
        if (entry == null) {
            return null;
        }
        String[] parts = entry.split("=", 2);
        if (parts.length != 2) {
            return null;
        }
        ResourceLocation id = parseId(parts[0]);
        if (id == null) {
            return null;
        }
        int amplifier = Config.parseAmplifier(parts[1].trim());
        if (amplifier < 0) {
            return null;
        }
        return new Cap(id.toString(), amplifier);
    }

    private static BypassEntry parseBypass(String entry) {
        if (entry == null) {
            return null;
        }
        String[] effectAndItems = entry.split("\\|", 2);
        if (effectAndItems.length != 2) {
            return null;
        }
        ResourceLocation effectId = parseId(effectAndItems[0]);
        if (effectId == null) {
            return null;
        }

        Set<String> itemIds = new HashSet<>();
        for (String rawItem : effectAndItems[1].split(";")) {
            ResourceLocation itemId = parseId(rawItem);
            if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                return null;
            }
            itemIds.add(itemId.toString());
        }

        if (itemIds.isEmpty()) {
            return null;
        }
        return new BypassEntry(effectId.toString(), itemIds);
    }

    private record Cap(String id, int amplifier) {
    }

    private record BypassEntry(String effectId, Set<String> itemIds) {
    }
}
