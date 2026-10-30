package com.frost.lockdown;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Messages {

    private Messages() {
    }

    public static String get(ModConfigSpec.ConfigValue<String> key, Object... placeholders) {
        return format(key.get(), placeholders);
    }

    public static String format(String template, Object... placeholders) {
        if (template == null) {
            return "";
        }
        String result = template;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            String name = String.valueOf(placeholders[i]);
            String value = String.valueOf(placeholders[i + 1]);
            result = result.replace("%" + name + "%", value);
        }
        return result;
    }

    public static MutableComponent component(ModConfigSpec.ConfigValue<String> key, Object... placeholders) {
        return Component.literal(get(key, placeholders));
    }

    public static MutableComponent red(ModConfigSpec.ConfigValue<String> key, Object... placeholders) {
        return component(key, placeholders).withStyle(ChatFormatting.RED);
    }

    public static void send(Player player, ModConfigSpec.ConfigValue<String> key, Object... placeholders) {
        player.displayClientMessage(red(key, placeholders), true);
    }
}
