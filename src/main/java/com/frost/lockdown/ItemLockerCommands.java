package com.frost.lockdown;

import java.util.List;
import java.util.Optional;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ItemLocker.MODID) 
public class ItemLockerCommands {

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

        dispatcher.register(Commands.literal("itemlocker")
                .requires(source -> source.hasPermission(4))

                .then(Commands.literal("prohibitedList")
                    .then(Commands.literal("addFromHand")
                        .executes(ItemLockerCommands::addProhibitedItemFromHand))
                    .then(Commands.literal("removeFromHand")
                        .executes(ItemLockerCommands::removeProhibitedItemFromHand))
                    .then(Commands.literal("list")
                        .executes(ItemLockerCommands::listProhibitedItems)))

                .then(Commands.literal("level")
                    .then(Commands.literal("item")
                        .then(Commands.literal("addFromHand")
                            .then(Commands.argument("level", IntegerArgumentType.integer())
                                .executes(ItemLockerCommands::addItemLevelFromHand)))
                        .then(Commands.literal("removeFromHand")
                            .executes(ItemLockerCommands::removeItemLevelFromHand))
                        .then(Commands.literal("list")
                            .executes(ItemLockerCommands::listItemLevels)))

                    .then(Commands.literal("mod")
                        .then(Commands.literal("addFromHand")
                            .then(Commands.argument("level", IntegerArgumentType.integer())
                                .executes(ItemLockerCommands::addModFromHand)))
                        .then(Commands.literal("removeFromHand")
                            .executes(ItemLockerCommands::removeModFromHand))
                        .then(Commands.literal("addExcludeFromHand")
                            .executes(ItemLockerCommands::addExcludeFromHand))
                        .then(Commands.literal("removeExcludeFromHand")
                            .executes(ItemLockerCommands::removeExcludeFromHand))
                        .then(Commands.literal("list")
                            .executes(ItemLockerCommands::listModIdLevels))
                        .then(Commands.literal("listExclude")
                            .executes(ItemLockerCommands::listExcludeModIdLevels))))

                .then(Commands.literal("blockedList")
                    .then(Commands.literal("addFromHand")
                        .executes(ItemLockerCommands::addBlockedItemFromHand))
                    .then(Commands.literal("removeFromHand")
                        .executes(ItemLockerCommands::removeBlockedItemFromHand))
                    .then(Commands.literal("list")
                        .executes(ItemLockerCommands::listBlockedItems)))

                .then(Commands.literal("scoreboard")
                    .then(Commands.literal("set")
                        .then(Commands.argument("scoreboard", StringArgumentType.word())
                            .suggests(SCOREBOARD_SUGGESTIONS)
                            .executes(ItemLockerCommands::setScoreboard)))
                    .then(Commands.literal("get")
                        .executes(ItemLockerCommands::getScoreboard)))

                .then(Commands.literal("locktag")
                    .then(Commands.literal("set")
                        .then(Commands.argument("tag", StringArgumentType.string())
                            .executes(ItemLockerCommands::setLockTag)))
                    .then(Commands.literal("get")
                        .executes(ItemLockerCommands::getLockTag)))
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

    // === CREATIVE ITEMS ===

    private static int listProhibitedItems(CommandContext<CommandSourceStack> ctx) {
        List<? extends String> current = Config.PROHIBITED_ITEM_IDS.get();
        ctx.getSource().sendSuccess(() -> Component.literal("Креативные предметы: " + current), false);
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

    // === BLOCKED ITEMS ===

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

    // === ITEM LEVELS ===

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

    // === SCOREBOARD ===

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

    // === LOCK TAG ===

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

    // === MODS ===

    public static int addModId(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "item");
        String modId = id.getNamespace();
        int level = IntegerArgumentType.getInteger(ctx, "level");

        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            ctx.getSource().sendFailure(Component.literal("Предмет не найден: " + id));
            return 0;
        }

        ItemLockerService.setKeyValue(
                Config.MOD_LEVELS_RAW::get,
                Config.MOD_LEVELS_RAW::set,
                Config.MOD_LEVELS_RAW::save,
                Config::rebuildModLevelIdsCache,
                modId,
                level
        );

        ctx.getSource().sendSuccess(() -> Component.literal(modId + " -> уровень " + level), true);
        return 1;
    }

    public static int removeModId(CommandContext<CommandSourceStack> ctx) {
        String modId = ResourceLocationArgument.getId(ctx, "item").getNamespace();

        boolean success = ItemLockerService.removeKeyValue(
                Config.MOD_LEVELS_RAW::get,
                Config.MOD_LEVELS_RAW::set,
                Config.MOD_LEVELS_RAW::save,
                Config::rebuildModLevelIdsCache,
                modId
        );

        if (!success) {
            ctx.getSource().sendFailure(Component.literal("Для " + modId + " уровень не задан."));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Уровень для " + modId + " удалён."), true);
        return 1;
    }

    public static int addModFromHand(CommandContext<CommandSourceStack> ctx) {
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

    public static int removeModFromHand(CommandContext<CommandSourceStack> ctx) {
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

    public static int listModIdLevels(CommandContext<CommandSourceStack> ctx) {
        List<? extends String> current = Config.MOD_LEVELS_RAW.get();
        ctx.getSource().sendSuccess(() -> Component.literal("Уровни модов: " + current), false);
        return current.size();
    }

    // === EXCLUDES ===

    public static int addExcludeModId(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "item");
        String itemId = id.toString();

        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            ctx.getSource().sendFailure(Component.literal("Предмет не найден: " + itemId));
            return 0;
        }

        boolean success = ItemLockerService.addToSimpleList(
                Config.EXCLUDE_ITEM_IDS::get,
                Config.EXCLUDE_ITEM_IDS::set,
                Config.EXCLUDE_ITEM_IDS::save,
                Config::rebuildExcludeItemIdsCache,
                itemId
        );

        if (!success) {
            ctx.getSource().sendFailure(Component.literal(itemId + " уже в списке исключений."));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Добавлено в исключения: " + itemId), true);
        return 1;
    }

    public static int removeExcludeModId(CommandContext<CommandSourceStack> ctx) {
        String itemId = ResourceLocationArgument.getId(ctx, "item").toString();

        boolean success = ItemLockerService.removeFromSimpleList(
                Config.EXCLUDE_ITEM_IDS::get,
                Config.EXCLUDE_ITEM_IDS::set,
                Config.EXCLUDE_ITEM_IDS::save,
                Config::rebuildExcludeItemIdsCache,
                itemId
        );

        if (!success) {
            ctx.getSource().sendFailure(Component.literal(itemId + " не найден в списке исключений."));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Удалено из исключений: " + itemId), true);
        return 1;
    }

    public static int addExcludeFromHand(CommandContext<CommandSourceStack> ctx) {
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

    public static int removeExcludeFromHand(CommandContext<CommandSourceStack> ctx) {
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

    public static int listExcludeModIdLevels(CommandContext<CommandSourceStack> ctx) {
        List<? extends String> current = Config.EXCLUDE_ITEM_IDS.get();
        ctx.getSource().sendSuccess(() -> Component.literal("Исключения: " + current), false);
        return current.size();
    }
}