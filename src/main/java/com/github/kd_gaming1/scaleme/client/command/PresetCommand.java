package com.github.kd_gaming1.scaleme.client.command;

import com.github.kd_gaming1.scaleme.Scaleme;
import com.github.kd_gaming1.scaleme.client.data.PlayerPreset;
import com.github.kd_gaming1.scaleme.client.gui.ScaleMeManagerScreen;
import com.github.kd_gaming1.scaleme.client.util.PlayerPresetManager;
import com.github.kd_gaming1.scaleme.client.util.PlayerUUIDResolver;
import com.github.kd_gaming1.scaleme.client.util.ScaleConstants;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

/**
 * Command handler for ScaleMe preset management and GUI access.
 * <p>
 * Available commands:
 * <ul>
 *   <li>/scaleme add &lt;player&gt; &lt;scale&gt; - Add a new preset</li>
 *   <li>/scaleme remove &lt;player&gt; - Remove a preset</li>
 *   <li>/scaleme list - List all presets</li>
 *   <li>/scaleme enable &lt;player&gt; - Enable a preset</li>
 *   <li>/scaleme disable &lt;player&gt; - Disable a preset</li>
 *   <li>/scaleme reload - Reload presets from disk</li>
 *   <li>/scaleme info &lt;player&gt; - Show preset information</li>
 *   <li>/scaleme setdefault &lt;scale&gt; - Set default scale for other players</li>
 *   <li>/scaleme setown &lt;scale&gt; - Set your own player scale</li>
 *   <li>/scaleme config - Open configuration menu</li>
 *   <li>/scaleme manager - Open preset manager GUI</li>
 *   <li>/scaleme gui - Open preset manager GUI (alias)</li>
 * </ul>
 */
public class PresetCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("scaleme")
                    .executes(PresetCommand::executeOpenConfig)
                    // Preset management commands
                    .then(literal("add")
                            .then(argument("player", StringArgumentType.string())
                                    .then(argument("scale", FloatArgumentType.floatArg(
                                            ScaleConstants.MIN_SCALE,
                                            ScaleConstants.MAX_SCALE))
                                            .executes(PresetCommand::executeAdd))))

                    .then(literal("remove")
                            .then(argument("player", StringArgumentType.string())
                                    .executes(PresetCommand::executeRemove)))

                    .then(literal("list")
                            .executes(PresetCommand::executeList))

                    .then(literal("enable")
                            .then(argument("player", StringArgumentType.string())
                                    .executes(PresetCommand::executeEnable)))

                    .then(literal("disable")
                            .then(argument("player", StringArgumentType.string())
                                    .executes(PresetCommand::executeDisable)))

                    .then(literal("reload")
                            .executes(PresetCommand::executeReload))

                    .then(literal("info")
                            .then(argument("player", StringArgumentType.string())
                                    .executes(PresetCommand::executeInfo)))

                    // Configuration commands
                    .then(literal("setdefault")
                            .then(argument("scale", FloatArgumentType.floatArg(
                                    ScaleConstants.MIN_SCALE,
                                    ScaleConstants.MAX_SCALE))
                                    .executes(PresetCommand::executeSetDefault)))

                    .then(literal("setown")
                            .then(argument("scale", FloatArgumentType.floatArg(
                                    ScaleConstants.MIN_SCALE,
                                    ScaleConstants.MAX_SCALE))
                                    .executes(PresetCommand::executeSetOwn)))

                    // GUI commands
                    .then(literal("config")
                            .executes(PresetCommand::executeOpenConfig))

                    .then(literal("manager")
                            .executes(PresetCommand::executeOpenManager))

                    .then(literal("gui")
                            .executes(PresetCommand::executeOpenManager))
            );
        });
    }

    // ===== Preset Management Commands =====

    /**
     * Adds a new preset for a player.
     */
    private static int executeAdd(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(ctx, "player");
        float scale = FloatArgumentType.getFloat(ctx, "scale");

        // Validate scale
        if (!ScaleConstants.isValidScale(scale)) {
            sendError(ctx, String.format("Scale must be between %.1f and %.1f",
                    ScaleConstants.MIN_SCALE, ScaleConstants.MAX_SCALE));
            return 0;
        }

        // Resolve player UUID
        UUID playerUUID = resolvePlayerUUID(playerName);
        if (playerUUID == null) {
            sendError(ctx, "Player '" + playerName + "' not found or never joined this server.");
            return 0;
        }

        // Check if preset already exists
        String uuidString = playerUUID.toString();
        PlayerPreset existing = PlayerPresetManager.getPreset(uuidString);
        if (existing != null) {
            sendError(ctx, "Preset for player '" + playerName + "' already exists. Use remove first to overwrite.");
            return 0;
        }

        // Create and add preset
        PlayerPreset preset = new PlayerPreset(uuidString, playerName, scale);
        PlayerPresetManager.addOrUpdatePreset(preset);

        sendSuccess(ctx, String.format("Added preset for '%s' with scale %.2f", playerName, scale));
        return 1;
    }

    /**
     * Removes a preset for a player.
     */
    private static int executeRemove(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(ctx, "player");

        UUID playerUUID = resolvePlayerUUID(playerName);
        if (playerUUID == null) {
            sendError(ctx, "Player '" + playerName + "' not found.");
            return 0;
        }

        String uuidString = playerUUID.toString();
        boolean removed = PlayerPresetManager.removePreset(uuidString);

        if (removed) {
            sendSuccess(ctx, "Removed preset for '" + playerName + "'.");
            return 1;
        } else {
            sendError(ctx, "No preset found for '" + playerName + "'.");
            return 0;
        }
    }

    /**
     * Lists all presets.
     */
    private static int executeList(CommandContext<FabricClientCommandSource> ctx) {
        List<PlayerPreset> presets = PlayerPresetManager.getAllPresets();

        if (presets.isEmpty()) {
            sendInfo(ctx, "No presets found.");
            return 1;
        }

        sendInfo(ctx, "Player Scaling Presets:");
        for (PlayerPreset preset : presets) {
            String status = preset.enabled ? "§a✓" : "§c✗";
            String name = preset.getEffectiveDisplayName();
            String scaleColor = getScaleColor(preset.scale);

            sendInfo(ctx, String.format("  %s §f%s§r: %s%.2f§r",
                    status, name, scaleColor, preset.scale));
        }

        return 1;
    }

    /**
     * Enables a preset.
     */
    private static int executeEnable(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(ctx, "player");

        UUID playerUUID = resolvePlayerUUID(playerName);
        if (playerUUID == null) {
            sendError(ctx, "Player '" + playerName + "' not found.");
            return 0;
        }

        String uuidString = playerUUID.toString();
        boolean success = PlayerPresetManager.setPresetEnabled(uuidString, true);

        if (success) {
            sendSuccess(ctx, "Enabled preset for '" + playerName + "'.");
            return 1;
        } else {
            sendError(ctx, "No preset found for '" + playerName + "'.");
            return 0;
        }
    }

    /**
     * Disables a preset.
     */
    private static int executeDisable(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(ctx, "player");

        UUID playerUUID = resolvePlayerUUID(playerName);
        if (playerUUID == null) {
            sendError(ctx, "Player '" + playerName + "' not found.");
            return 0;
        }

        String uuidString = playerUUID.toString();
        boolean success = PlayerPresetManager.setPresetEnabled(uuidString, false);

        if (success) {
            sendSuccess(ctx, "Disabled preset for '" + playerName + "'.");
            return 1;
        } else {
            sendError(ctx, "No preset found for '" + playerName + "'.");
            return 0;
        }
    }

    /**
     * Reloads all presets from disk.
     */
    private static int executeReload(CommandContext<FabricClientCommandSource> ctx) {
        PlayerPresetManager.loadPresets();
        sendSuccess(ctx, "Reloaded all presets from disk.");
        return 1;
    }

    /**
     * Shows detailed information about a preset.
     */
    private static int executeInfo(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(ctx, "player");

        UUID playerUUID = resolvePlayerUUID(playerName);
        if (playerUUID == null) {
            sendError(ctx, "Player '" + playerName + "' not found.");
            return 0;
        }

        String uuidString = playerUUID.toString();
        PlayerPreset preset = PlayerPresetManager.getPreset(uuidString);

        if (preset == null) {
            sendError(ctx, "No preset found for '" + playerName + "'.");
            return 0;
        }

        sendInfo(ctx, "Preset Information for '" + playerName + "':");
        sendInfo(ctx, "  UUID: " + preset.identifier);
        sendInfo(ctx, "  Display Name: " + preset.getEffectiveDisplayName());
        sendInfo(ctx, "  Scale: " + preset.scale);
        sendInfo(ctx, "  Status: " + (preset.enabled ? "§aEnabled§r" : "§cDisabled§r"));

        return 1;
    }

    // ===== Configuration Commands =====

    /**
     * Sets the default scale for other players.
     */
    private static int executeSetDefault(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        float scale = FloatArgumentType.getFloat(ctx, "scale");

        if (!ScaleConstants.isValidScale(scale)) {
            sendError(ctx, String.format("Scale must be between %.1f and %.1f",
                    ScaleConstants.MIN_SCALE, ScaleConstants.MAX_SCALE));
            return 0;
        }

        ScaleMeConfig.otherPlayersScale = ScaleConstants.clampScale(scale);
        sendSuccess(ctx, String.format("Set default scale for other players to %.2f", scale));

        return 1;
    }

    /**
     * Sets the scale for the player's own character.
     */
    private static int executeSetOwn(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        float scale = FloatArgumentType.getFloat(ctx, "scale");

        if (!ScaleConstants.isValidScale(scale)) {
            sendError(ctx, String.format("Scale must be between %.1f and %.1f",
                    ScaleConstants.MIN_SCALE, ScaleConstants.MAX_SCALE));
            return 0;
        }

        ScaleMeConfig.ownPlayerScale = ScaleConstants.clampScale(scale);
        sendSuccess(ctx, String.format("Set your own player scale to %.2f", scale));

        return 1;
    }

    // ===== GUI Commands =====

    /**
     * Opens the configuration menu.
     * Uses client.send() to delay opening until after chat closes.
     */
    private static int executeOpenConfig(CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            sendError(ctx, "You must be in-game to open the config menu.");
            return 0;
        }

        // Schedule GUI opening on next tick (after chat closes)
        client.send(() -> {
            try {
                client.setScreen(MidnightConfig.getScreen(client.currentScreen, Scaleme.MOD_ID));
            } catch (Exception e) {
                Scaleme.LOGGER.error("Failed to open config menu", e);
            }
        });

        sendSuccess(ctx, "Opening configuration menu...");
        return 1;
    }

    /**
     * Opens the preset manager GUI.
     * Uses client.send() to delay opening until after chat closes.
     */
    private static int executeOpenManager(CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            sendError(ctx, "You must be in-game to open the preset manager.");
            return 0;
        }

        // Schedule GUI opening on next tick (after chat closes)
        client.send(() -> {
            try {
                client.setScreen(new ScaleMeManagerScreen());
            } catch (Exception e) {
                Scaleme.LOGGER.error("Failed to open preset manager", e);
            }
        });

        sendSuccess(ctx, "Opening preset manager...");
        return 1;
    }

    // ===== Helper Methods =====

    /**
     * Resolves a player's UUID from their username.
     */
    private static UUID resolvePlayerUUID(String playerName) {
        return PlayerUUIDResolver.resolvePlayerUUID(playerName);
    }

    /**
     * Returns a color code for a scale value based on its magnitude.
     */
    private static String getScaleColor(float scale) {
        if (ScaleConstants.isTinyScale(scale)) {
            return "§9"; // Blue for tiny
        } else if (ScaleConstants.isLargeScale(scale)) {
            return "§c"; // Red for large
        } else {
            return "§a"; // Green for normal
        }
    }

    /**
     * Sends a success message to the player.
     */
    private static void sendSuccess(CommandContext<FabricClientCommandSource> ctx, String message) {
        ctx.getSource().sendFeedback(Text.literal("§a[ScaleMe] " + message));
    }

    /**
     * Sends an error message to the player.
     */
    private static void sendError(CommandContext<FabricClientCommandSource> ctx, String message) {
        ctx.getSource().sendError(Text.literal("§c[ScaleMe] " + message));
    }

    /**
     * Sends an informational message to the player.
     */
    private static void sendInfo(CommandContext<FabricClientCommandSource> ctx, String message) {
        ctx.getSource().sendFeedback(Text.literal("§b[ScaleMe] " + message));
    }
}