package com.frost.lockdown;

import java.util.List;
import java.util.Optional;

import com.frost.lockdown.effect.EffectLockerConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Lockdown.MODID)
public class LockdownCommands {

    private static final SuggestionProvider<CommandSourceStack> ITEM_SUGGESTIONS =
            (context, builder) -> {
                BuiltInRegistries.ITEM.keySet().forEach(id -> builder.suggest(id.toString()));
                return builder.buildFuture();
            };

    private static final SuggestionProvider<CommandSourceStack> SCOREBOARD_SUGGESTIONS =
            (context, builder) -> {
                Scoreboard scoreboard = context.getSource().getServer().getScoreboard();
                scoreboard.getObjectiveNames().forEach(builder::suggest);
                return builder.buildFuture();
            };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("lockdown")
                .requires(source -> source.hasPermission(4))

                .then(Commands.literal("item")
                    .then(Commands.literal("prohibitedList")
                        .then(Commands.literal("addFromHand")
                            .executes(LockdownCommands::addProhibitedItemFromHand))
                        .then(Commands.literal("removeFromHand")
                            .executes(LockdownCommands::removeProhibitedItemFromHand))
                        .then(Commands.literal("list")
                            .executes(LockdownCommands::listProhibitedItems)))

                    .then(Commands.literal("level")
                        .then(Commands.literal("item")
                            .then(Commands.literal("addFromHand")
                                .then(Commands.argument("level", IntegerArgumentType.integer())
                                    .executes(LockdownCommands::addItemLevelFromHand)))
                            .then(Commands.literal("removeFromHand")
                                .executes(LockdownCommands::removeItemLevelFromHand))
                            .then(Commands.literal("list")
                                .executes(LockdownCommands::listItemLevels)))

                        .then(Commands.literal("mod")
                            .then(Commands.literal("addFromHand")
                                .then(Commands.argument("level", IntegerArgumentType.integer())
                                    .executes(LockdownCommands::addModFromHand)))
                            .then(Commands.literal("removeFromHand")
                                .executes(LockdownCommands::removeModFromHand))
                            .then(Commands.literal("addExcludeFromHand")
                                .executes(LockdownCommands::addExcludeFromHand))
                            .then(Commands.literal("removeExcludeFromHand")
                                .executes(LockdownCommands::removeExcludeFromHand))
                            .then(Commands.literal("list")
                                .executes(LockdownCommands::listModIdLevels))
                            .then(Commands.literal("listExclude")
                                .executes(LockdownCommands::listExcludeModIdLevels))))

                    .then(Commands.literal("blockedList")
                        .then(Commands.literal("addFromHand")
                            .executes(LockdownCommands::addBlockedItemFromHand))
                        .then(Commands.literal("removeFromHand")
                            .executes(LockdownCommands::removeBlockedItemFromHand))
                        .then(Commands.literal("list")
                            .executes(LockdownCommands::listBlockedItems))))

                .then(Commands.literal("effect")
                    .then(Commands.literal("ban")
                        .then(Commands.argument("effect",
                                        ResourceArgument.resource(event.getBuildContext(), Registries.MOB_EFFECT))
                            .executes(LockdownCommands::banEffect)))
                    .then(Commands.literal("unban")
                        .then(Commands.argument("effect",
                                        ResourceArgument.resource(event.getBuildContext(), Registries.MOB_EFFECT))
                            .executes(LockdownCommands::unbanEffect)))
                    .then(Commands.literal("cap")
                        .then(Commands.argument("effect",
                                        ResourceArgument.resource(event.getBuildContext(), Registries.MOB_EFFECT))
                            .then(Commands.argument("level", IntegerArgumentType.integer(0, 255))
                                .executes(LockdownCommands::capEffect))))
                    .then(Commands.literal("uncap")
                        .then(Commands.argument("effect",
                                        ResourceArgument.resource(event.getBuildContext(), Registries.MOB_EFFECT))
                            .executes(LockdownCommands::uncapEffect)))
                    .then(Commands.literal("bypass")
                        .then(Commands.literal("add")
                            .then(Commands.argument("effect",
                                            ResourceArgument.resource(event.getBuildContext(), Registries.MOB_EFFECT))
                                .then(Commands.argument("item", StringArgumentType.string())
                                    .suggests(ITEM_SUGGESTIONS)
                                    .executes(LockdownCommands::addBypass))))
                        .then(Commands.literal("remove")
                            .then(Commands.argument("effect",
                                            ResourceArgument.resource(event.getBuildContext(), Registries.MOB_EFFECT))
                                .then(Commands.argument("item", StringArgumentType.string())
                                    .suggests(ITEM_SUGGESTIONS)
                                    .executes(LockdownCommands::removeBypass)))))
                    .then(Commands.literal("clear")
                        .executes(LockdownCommands::clearEffects))
                    .then(Commands.literal("list")
                        .executes(LockdownCommands::listEffects)))

                .then(Commands.literal("lockNBT")
                    .then(Commands.literal("set")
                        .then(Commands.argument("tag", StringArgumentType.string())
                            .executes(LockdownCommands::setLockTag)))
                    .then(Commands.literal("get")
                        .executes(LockdownCommands::getLockTag)))

                .then(Commands.literal("scoreboard")
                    .then(Commands.literal("set")
                        .then(Commands.argument("scoreboard", StringArgumentType.word())
                            .suggests(SCOREBOARD_SUGGESTIONS)
                            .executes(LockdownCommands::setScoreboard)))
                    .then(Commands.literal("get")
                        .executes(LockdownCommands::getScoreboard)))
        );
    }

    private static Optional<String> getItemIdFromHand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Эту команду может выполнить только игрок."));
            return Optional.empty();
        }
        ItemStack stack = player.getMainHandItem();
        Optional<ResourceLocation> location = ItemLockerService.getItemLocation(stack);
        if (location.isEmpty()) {
            source.sendFailure(Component.literal("В руке ничего нет."));
            return Optional.empty();
        }
        return location.map(ResourceLocation::toString);
    }

    private static Optional<String> getModIdFromHand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Эту команду может выполнить только игрок."));
            return Optional.empty();
        }
        ItemStack stack = player.getMainHandItem();
        Optional<ResourceLocation> location = ItemLockerService.getItemLocation(stack);
        if (location.isEmpty()) {
            source.sendFailure(Component.literal("В руке ничего нет."));
            return Optional.empty();
        }
        return location.map(ResourceLocation::getNamespace);
    }


    private static int listProhibitedItems(CommandContext<CommandSourceStack> ctx) {
        List<? extends String> current = Config.PROHIBITED_ITEM_IDS.get();
        ctx.getSource().sendSuccess(() -> Component.literal("Запрещённые предметы: " + current), false);
        return current.size();
    }

    private static int addProhibitedItemFromHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        return getItemIdFromHand(source).map(itemId -> {
            boolean success = ItemLockerService.addToSimpleList(
                    Config.PROHIBITED_ITEM_IDS::get,
                    Config.PROHIBITED_ITEM_IDS::set,
                    Config.PROHIBITED_ITEM_IDS::save,
                    Config::rebuildProhibitedItemIdsCache,
                    itemId
            );
            if (!success) {
                source.sendFailure(Component.literal(itemId + " уже в списке."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Добавлено: " + itemId), true);
            return 1;
        }).orElse(0);
    }

    private static int removeProhibitedItemFromHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        return getItemIdFromHand(source).map(itemId -> {
            boolean success = ItemLockerService.removeFromSimpleList(
                    Config.PROHIBITED_ITEM_IDS::get,
                    Config.PROHIBITED_ITEM_IDS::set,
                    Config.PROHIBITED_ITEM_IDS::save,
                    Config::rebuildProhibitedItemIdsCache,
                    itemId
            );
            if (!success) {
                source.sendFailure(Component.literal(itemId + " нет в списке."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Удалено: " + itemId), true);
            return 1;
        }).orElse(0);
    }

    private static int listBlockedItems(CommandContext<CommandSourceStack> ctx) {
        List<? extends String> current = Config.BLOCKED_ITEM_IDS.get();
        ctx.getSource().sendSuccess(() -> Component.literal("Заблокированные предметы: " + current), false);
        return current.size();
    }

    private static int addBlockedItemFromHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        return getItemIdFromHand(source).map(itemId -> {
            boolean success = ItemLockerService.addToSimpleList(
                    Config.BLOCKED_ITEM_IDS::get,
                    Config.BLOCKED_ITEM_IDS::set,
                    Config.BLOCKED_ITEM_IDS::save,
                    Config::rebuildBlockedItemIdsCache,
                    itemId
            );
            if (!success) {
                source.sendFailure(Component.literal(itemId + " уже в списке."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Добавлено: " + itemId), true);
            return 1;
        }).orElse(0);
    }

    private static int removeBlockedItemFromHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        return getItemIdFromHand(source).map(itemId -> {
            boolean success = ItemLockerService.removeFromSimpleList(
                    Config.BLOCKED_ITEM_IDS::get,
                    Config.BLOCKED_ITEM_IDS::set,
                    Config.BLOCKED_ITEM_IDS::save,
                    Config::rebuildBlockedItemIdsCache,
                    itemId
            );
            if (!success) {
                source.sendFailure(Component.literal(itemId + " нет в списке."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Удалено: " + itemId), true);
            return 1;
        }).orElse(0);
    }

    private static int listItemLevels(CommandContext<CommandSourceStack> ctx) {
        List<? extends String> current = Config.ITEM_LEVELS_RAW.get();
        ctx.getSource().sendSuccess(() -> Component.literal("Уровни предметов: " + current), false);
        return current.size();
    }

    private static int addItemLevelFromHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        int level = IntegerArgumentType.getInteger(ctx, "level");

        return getItemIdFromHand(source).map(itemId -> {
            ItemLockerService.setKeyValue(
                    Config.ITEM_LEVELS_RAW::get,
                    Config.ITEM_LEVELS_RAW::set,
                    Config.ITEM_LEVELS_RAW::save,
                    Config::rebuildItemLevelIdsCache,
                    itemId,
                    level
            );
            source.sendSuccess(() -> Component.literal("Добавлено: " + itemId + " -> " + level), true);
            return 1;
        }).orElse(0);
    }

    private static int removeItemLevelFromHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        return getItemIdFromHand(source).map(itemId -> {
            boolean success = ItemLockerService.removeKeyValue(
                    Config.ITEM_LEVELS_RAW::get,
                    Config.ITEM_LEVELS_RAW::set,
                    Config.ITEM_LEVELS_RAW::save,
                    Config::rebuildItemLevelIdsCache,
                    itemId
            );
            if (!success) {
                source.sendSuccess(() -> Component.literal("Предмет " + itemId + " уже отсутствует."), true);
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Удалено: " + itemId), true);
            return 1;
        }).orElse(0);
    }


    private static int addModFromHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        int level = IntegerArgumentType.getInteger(ctx, "level");

        return getModIdFromHand(source).map(modId -> {
            ItemLockerService.setKeyValue(
                    Config.MOD_LEVELS_RAW::get,
                    Config.MOD_LEVELS_RAW::set,
                    Config.MOD_LEVELS_RAW::save,
                    Config::rebuildModLevelIdsCache,
                    modId,
                    level
            );
            source.sendSuccess(() -> Component.literal("Добавлен мод: " + modId + " -> " + level), true);
            return 1;
        }).orElse(0);
    }

    private static int removeModFromHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        return getModIdFromHand(source).map(modId -> {
            boolean success = ItemLockerService.removeKeyValue(
                    Config.MOD_LEVELS_RAW::get,
                    Config.MOD_LEVELS_RAW::set,
                    Config.MOD_LEVELS_RAW::save,
                    Config::rebuildModLevelIdsCache,
                    modId
            );
            if (!success) {
                source.sendSuccess(() -> Component.literal("Мод " + modId + " уже отсутствует."), true);
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Удалено: " + modId), true);
            return 1;
        }).orElse(0);
    }

    private static int listModIdLevels(CommandContext<CommandSourceStack> ctx) {
        List<? extends String> current = Config.MOD_LEVELS_RAW.get();
        ctx.getSource().sendSuccess(() -> Component.literal("Уровни модов: " + current), false);
        return current.size();
    }

    private static int addExcludeFromHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        return getItemIdFromHand(source).map(itemId -> {
            boolean success = ItemLockerService.addToSimpleList(
                    Config.EXCLUDE_ITEM_IDS::get,
                    Config.EXCLUDE_ITEM_IDS::set,
                    Config.EXCLUDE_ITEM_IDS::save,
                    Config::rebuildExcludeItemIdsCache,
                    itemId
            );
            if (!success) {
                source.sendFailure(Component.literal(itemId + " уже в списке исключений."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Добавлен в исключения: " + itemId), true);
            return 1;
        }).orElse(0);
    }

    private static int removeExcludeFromHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        return getItemIdFromHand(source).map(itemId -> {
            boolean success = ItemLockerService.removeFromSimpleList(
                    Config.EXCLUDE_ITEM_IDS::get,
                    Config.EXCLUDE_ITEM_IDS::set,
                    Config.EXCLUDE_ITEM_IDS::save,
                    Config::rebuildExcludeItemIdsCache,
                    itemId
            );
            if (!success) {
                source.sendFailure(Component.literal(itemId + " нет в списке исключений."));
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Удалено из исключений: " + itemId), true);
            return 1;
        }).orElse(0);
    }

    private static int listExcludeModIdLevels(CommandContext<CommandSourceStack> ctx) {
        List<? extends String> current = Config.EXCLUDE_ITEM_IDS.get();
        ctx.getSource().sendSuccess(() -> Component.literal("Исключения: " + current), false);
        return current.size();
    }


    private static int setScoreboard(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerScoreboard serverBoard = source.getServer().getScoreboard();
        String scoreboardName = context.getArgument("scoreboard", String.class);

        if (serverBoard.getObjective(scoreboardName) == null) {
            source.sendFailure(Component.literal("§cСкорборд с именем '" + scoreboardName + "' не найден!"));
            return 0;
        }

        Config.SCOREBOARD_NAME.set(scoreboardName);
        Config.SCOREBOARD_NAME.save();

        source.sendSuccess(() -> Component.literal("§aСкорборд '" + scoreboardName + "' успешно установлен!"), true);
        return 1;
    }

    private static int getScoreboard(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = Config.SCOREBOARD_NAME.get();
        if (name != null && !name.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Активный скорборд: " + name), true);
            return 1;
        }
        return 0;
    }


    private static int setLockTag(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String tag = StringArgumentType.getString(context, "tag");

        Config.LOCK_NBT_TAG.set(tag);
        Config.LOCK_NBT_TAG.save();

        source.sendSuccess(() -> Component.literal("Тег блокировки установлен: " + tag), true);
        return 1;
    }

    private static int getLockTag(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String tag = Config.LOCK_NBT_TAG.get();
        if (tag != null && !tag.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Текущий тег блокировки: " + tag), true);
            return 1;
        }
        return 0;
    }


    private static int banEffect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Holder.Reference<MobEffect> holder = ResourceArgument.getMobEffect(ctx, "effect");
        String id = holder.key().location().toString();

        boolean added = EffectLockerConfig.addBanned(id);
        if (!added) {
            source.sendFailure(Component.literal("Эффект уже в списке запрещённых: " + id));
            return 0;
        }

        EffectLockerConfig.removeCap(id);

        int stripped = 0;
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            if (player.removeEffect(holder)) {
                stripped++;
            }
        }
        final int removedCount = stripped;

        source.sendSuccess(() -> Component.literal(Messages.get(Config.MSG_EFFECT_BANNED_CHAT,
                "effect", id, "count", removedCount)), true);
        return 1;
    }

    private static int unbanEffect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Holder.Reference<MobEffect> holder = ResourceArgument.getMobEffect(ctx, "effect");
        String id = holder.key().location().toString();

        boolean removedBan = EffectLockerConfig.removeBanned(id);
        boolean removedCap = EffectLockerConfig.removeCap(id);

        if (!removedBan && !removedCap) {
            source.sendFailure(Component.literal("Эффект не запрещён и не имеет ограничения уровня: " + id));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(Messages.get(Config.MSG_EFFECT_UNBANNED_CHAT, "effect", id)), true);
        return 1;
    }

    private static int capEffect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Holder.Reference<MobEffect> holder = ResourceArgument.getMobEffect(ctx, "effect");
        String id = holder.key().location().toString();
        int level = IntegerArgumentType.getInteger(ctx, "level");

        EffectLockerConfig.setCap(id, level);
        EffectLockerConfig.removeBanned(id);

        source.sendSuccess(() -> Component.literal(Messages.get(Config.MSG_EFFECT_CAPPED_CHAT,
                "effect", id, "level", level)), true);
        return 1;
    }

    private static int uncapEffect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Holder.Reference<MobEffect> holder = ResourceArgument.getMobEffect(ctx, "effect");
        String id = holder.key().location().toString();

        boolean removed = EffectLockerConfig.removeCap(id);
        if (!removed) {
            source.sendFailure(Component.literal("Для эффекта не задано ограничение уровня: " + id));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(Messages.get(Config.MSG_EFFECT_CAP_REMOVED_CHAT, "effect", id)), true);
        return 1;
    }

    private static int addBypass(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Holder.Reference<MobEffect> holder = ResourceArgument.getMobEffect(ctx, "effect");
        String effectId = holder.key().location().toString();
        String itemId = normalizeItemId(StringArgumentType.getString(ctx, "item"));

        if (itemId == null) {
            source.sendFailure(Component.literal("Предмет не найден."));
            return 0;
        }

        boolean added = EffectLockerConfig.addBypass(effectId, itemId);
        if (!added) {
            source.sendFailure(Component.literal("Этот предмет уже в bypass-списке эффекта: " + itemId));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(Messages.get(Config.MSG_BYPASS_ADDED_CHAT,
                "effect", effectId, "item", itemId)), true);
        return 1;
    }

    private static int removeBypass(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Holder.Reference<MobEffect> holder = ResourceArgument.getMobEffect(ctx, "effect");
        String effectId = holder.key().location().toString();
        String itemId = normalizeItemId(StringArgumentType.getString(ctx, "item"));

        if (itemId == null) {
            source.sendFailure(Component.literal("Предмет не найден."));
            return 0;
        }

        boolean removed = EffectLockerConfig.removeBypass(effectId, itemId);
        if (!removed) {
            source.sendFailure(Component.literal("Этот предмет отсутствует в bypass-списке эффекта: " + itemId));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(Messages.get(Config.MSG_BYPASS_REMOVED_CHAT,
                "effect", effectId, "item", itemId)), true);
        return 1;
    }

    private static int clearEffects(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        EffectLockerConfig.clearAll();
        source.sendSuccess(() -> Component.literal(Messages.get(Config.MSG_EFFECT_CLEARED_CHAT)), true);
        return 1;
    }

    private static int listEffects(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        List<String> banned = EffectLockerConfig.getBannedEffects();
        if (banned.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Список запрещённых эффектов пуст."), false);
        } else {
            source.sendSuccess(() -> Component.literal("Запрещённые эффекты (" + banned.size() + "):"), false);
            for (String id : banned) {
                source.sendSuccess(() -> Component.literal(" - " + id), false);
            }
        }

        List<String> capped = EffectLockerConfig.getCappedEffects();
        if (capped.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Ограничения уровня отсутствуют."), false);
        } else {
            source.sendSuccess(() -> Component.literal("Ограничения уровня (" + capped.size() + "):"), false);
            for (String entry : capped) {
                source.sendSuccess(() -> Component.literal(" - " + entry), false);
            }
        }

        var bypass = EffectLockerConfig.getBypassMap();
        if (bypass.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Bypass-исключения отсутствуют."), false);
        } else {
            source.sendSuccess(() -> Component.literal("Bypass-исключения (" + bypass.size() + "):"), false);
            for (var entry : bypass.entrySet()) {
                source.sendSuccess(() -> Component.literal(" - " + entry.getKey() + " | " + String.join(";", entry.getValue())), false);
            }
        }
        return 1;
    }

    private static String normalizeItemId(String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw.trim());
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return null;
        }
        return id.toString();
    }
}
