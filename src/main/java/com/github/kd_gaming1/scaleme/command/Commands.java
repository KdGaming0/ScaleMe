package com.github.kd_gaming1.scaleme.command;

import com.github.kd_gaming1.scaleme.ScaleMe;
import com.mojang.brigadier.context.CommandContext;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Commands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("scaleme")
                .executes(Commands::executeOpenConfig)
                .then(literal("config")
                        .executes(Commands::executeOpenConfig))
        ));
    }

    /**
     * Opens the configuration menu.
     * Uses client.send() to delay opening until after the chat closes.
     */
    private static int executeOpenConfig(CommandContext<FabricClientCommandSource> ctx) {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null) {
            sendError(ctx);
            return 0;
        }

        // Schedule GUI opening on the next tick (after chat closes)
        client.schedule(() -> {
            try {
                client.setScreen(MidnightConfig.getScreen(client.screen, ScaleMe.MOD_ID));
            } catch (Exception e) {
                ScaleMe.LOGGER.error("Failed to open config menu", e);
            }
        });

        sendSuccess(ctx);
        return 1;
    }

    /**
     * Sends a success message to the player.
     */
    private static void sendSuccess(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().sendFeedback(Component.literal("§a[Scale Me] " + "Opening configuration menu..."));
    }

    /**
     * Sends an error message to the player.
     */
    private static void sendError(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().sendError(Component.literal("§c[Scale Me] " + "You must be in-game to open the config menu."));
    }
}
