package com.github.kd_gaming1.scaleme.command;

import com.github.kd_gaming1.scaleme.ScaleMe;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.util.PresetManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
//?} else {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

//? if >=26.1 {
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
//?} else {
/*import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
*///?}

public class Commands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("scaleme")
                .executes(Commands::executeOpenConfig)
                .then(literal("config")
                        .executes(Commands::executeOpenConfig))
                .then(literal("export")
                        .executes(ctx -> executeExport(ctx, null))
                        .then(argument("category", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("hand");
                                    builder.suggest("anim");
                                    builder.suggest("scale");
                                    builder.suggest("view");
                                    builder.suggest("item");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String cat = StringArgumentType.getString(ctx, "category");
                                    return executeExport(ctx, resolveCategory(cat));
                                })))
                .then(literal("import")
                        .then(argument("json", StringArgumentType.greedyString())
                                .executes(Commands::executeImport)))
        ));
    }

    /**
     * Opens the configuration menu.
     * Uses client.schedule() to delay opening until after the chat closes.
     */
    private static int executeOpenConfig(CommandContext<FabricClientCommandSource> ctx) {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null) {
            sendError(ctx, "You must be in-game to open the config menu.");
            return 0;
        }

        client.schedule(() -> {
            try {
                //? if >=26.2 {
                client.gui.setScreen(MidnightConfig.getScreen(client.gui.screen(), ScaleMe.MOD_ID));
                //?} else {
                /*client.setScreen(MidnightConfig.getScreen(client.screen, ScaleMe.MOD_ID));
                *///?}
            } catch (IllegalStateException | NullPointerException e) {
                ScaleMe.LOGGER.error("Failed to open config menu: {}", e.getMessage());
            }
        });

        ctx.getSource().sendFeedback(Component.literal("§a[Scale Me] Opening configuration menu..."));
        return 1;
    }

    private static int executeExport(CommandContext<FabricClientCommandSource> ctx, String category) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            sendError(ctx, "You must be in-game to export presets.");
            return 0;
        }

        ctx.getSource().sendFeedback(PresetManager.exportToChat(category));
        return 1;
    }

    private static int executeImport(CommandContext<FabricClientCommandSource> ctx) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            sendError(ctx, "You must be in-game to import presets.");
            return 0;
        }

        String json = StringArgumentType.getString(ctx, "json");
        String result = PresetManager.importFromJson(json);
        ctx.getSource().sendFeedback(Component.literal(result));
        return 1;
    }

    /** Maps a user-facing category name to the internal MidnightConfig category constant. */
    private static String resolveCategory(String input) {
        return switch (input.toLowerCase()) {
            case "hand" -> ScaleMeConfig.HAND;
            case "anim", "animation" -> ScaleMeConfig.ANIM;
            case "scale", "scaling" -> ScaleMeConfig.SCALE;
            case "view", "camera" -> ScaleMeConfig.VIEW;
            case "item", "ground" -> ScaleMeConfig.ITEM;
            default -> null;
        };
    }

    private static void sendError(CommandContext<FabricClientCommandSource> ctx, String message) {
        ctx.getSource().sendError(Component.literal("§c[Scale Me] " + message));
    }
}