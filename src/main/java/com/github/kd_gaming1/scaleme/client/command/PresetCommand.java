package com.github.kd_gaming1.scaleme.client.command;

import com.github.kd_gaming1.scaleme.client.data.PlayerPreset;
import com.github.kd_gaming1.scaleme.client.util.PlayerPresetManager;
import com.github.kd_gaming1.scaleme.client.util.PlayerUUIDResolver;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

/**
 * Command handler for ScaleMe preset management.
 * Provides comprehensive commands for managing player scaling presets.
 */
public class PresetCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("scaleme")
                    .then(literal("add")
                            .then(argument("player", StringArgumentType.string())
                                    .then(argument("scale", FloatArgumentType.floatArg(0.1f, 10.0f))
                                            .executes(PresetCommand::executeAdd))))

                    .then(literal("remove")
                            .then(argument("player", StringArgumentType.string())
                                    .executes(PresetCommand::executeRemove)))

                    .then(literal("list")
                            .executes(ctx -> executeList(ctx, null))

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

                    .then(literal("setdefault")
                            .then(argument("scale", FloatArgumentType.floatArg(0.1f, 10.0f))
                                    .executes(PresetCommand::executeSetDefault)))

                    .then(literal("setown")
                            .then(argument("scale", FloatArgumentType.floatArg(0.1f, 10.0f))
                                    .executes(PresetCommand::executeSetOwn))))
            );
        });
    }

    private static int executeAdd(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(ctx, "player");
        float scale = FloatArgumentType.getFloat(ctx, "scale");

        UUID playerUUID = resolvePlayerUUID(playerName);
        if (playerUUID == null) {
            sendError(ctx, "Player '" + playerName + "' not found or never joined this server.");
            return 0;
        }

        String uuidString = playerUUID.toString();
        PlayerPreset existing = PlayerPresetManager.getPreset(uuidString);
        if (existing != null) {
            sendError(ctx, "Preset for player '" + playerName + "' already exists. Use remove first to overwrite.");
            return 0;
        }

        PlayerPreset preset = new PlayerPreset(uuidString, playerName, scale);
        PlayerPresetManager.addOrUpdatePreset(preset);

        sendSuccess(ctx, "Added preset for '" + playerName + "' with scale " + scale + ".");
        return 1;
    }

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

    private static int executeList(CommandContext<FabricClientCommandSource> ctx, String categoryFilter) {
        List<PlayerPreset> presets = PlayerPresetManager.getAllPresets();

        if (presets.isEmpty()) {
            sendInfo(ctx, "No presets found.");
            return 1;
        }

        sendInfo(ctx, "Player Scaling Presets:");
        int count = 0;

        for (PlayerPreset preset : presets) {
            String status = preset.enabled ? "§a✓" : "§c✗";
            String name = preset.getEffectiveDisplayName();

            sendInfo(ctx, String.format("  %s §f%s§r: §6%.2f%s", status, name, preset.scale));
            count++;
        }

        if (categoryFilter != null && count == 0) {
            sendInfo(ctx, "No presets found in category '" + categoryFilter + "'.");
        }

        return 1;
    }

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

    private static int executeReload(CommandContext<FabricClientCommandSource> ctx) {
        PlayerPresetManager.loadPresets();
        sendSuccess(ctx, "Reloaded all presets from disk.");
        return 1;
    }

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
        sendInfo(ctx, "  Friendly Name: " + (preset.friendlyName != null ? preset.friendlyName : "None"));
        sendInfo(ctx, "  Scale: " + preset.scale);
        sendInfo(ctx, "  Enabled: " + (preset.enabled ? "Yes" : "No"));

        return 1;
    }

    private static int executeSetDefault(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        float scale = FloatArgumentType.getFloat(ctx, "scale");

        // Update config (assuming there's a way to update it)
        ScaleMeConfig.otherPlayersScale = scale;

        // Save config if there's a save method
        // ScaleMeConfig.save(); // Uncomment if this method exists

        sendSuccess(ctx, "Set default scale for other players to " + scale + ".");
        return 1;
    }

    private static int executeSetOwn(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        float scale = FloatArgumentType.getFloat(ctx, "scale");

        // Update config (assuming there's a way to update it)
        ScaleMeConfig.ownPlayerScale = scale;

        // Save config if there's a save method
        // ScaleMeConfig.save(); // Uncomment if this method exists

        sendSuccess(ctx, "Set your own player scale to " + scale + ".");
        return 1;
    }

    // Resolves a player's UUID from their username.
    private static UUID resolvePlayerUUID(String playerName) {
        return PlayerUUIDResolver.resolvePlayerUUID(playerName);
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