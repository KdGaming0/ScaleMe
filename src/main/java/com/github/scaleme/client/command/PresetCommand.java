package com.github.scaleme.client.command;

import com.github.scaleme.client.data.PlayerPreset;
import com.github.scaleme.client.util.PlayerPresetManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PresetCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("scalepreset")
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("identifier", StringArgumentType.word())
                                .then(ClientCommandManager.argument("scale", FloatArgumentType.floatArg(0.1f, 3.0f))
                                        .executes(context -> {
                                            String identifier = StringArgumentType.getString(context, "identifier");
                                            float scale = FloatArgumentType.getFloat(context, "scale");

                                            // Create preset with identifier as display name and default category
                                            PlayerPreset preset = new PlayerPreset(identifier, null, scale, "command");

                                            PlayerPresetManager.addPreset(preset);

                                            context.getSource().sendFeedback(Text.literal("Added preset for: " + identifier + " (Scale: " + scale + "x)")
                                                    .formatted(Formatting.GREEN));

                                            return 1;
                                        })
                                        .then(ClientCommandManager.argument("displayName", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    String identifier = StringArgumentType.getString(context, "identifier");
                                                    float scale = FloatArgumentType.getFloat(context, "scale");
                                                    String displayName = StringArgumentType.getString(context, "displayName");

                                                    PlayerPreset preset = new PlayerPreset(identifier, displayName, scale, "command");

                                                    PlayerPresetManager.addPreset(preset);

                                                    context.getSource().sendFeedback(Text.literal("Added preset: " + displayName + " for " + identifier + " (Scale: " + scale + "x)")
                                                            .formatted(Formatting.GREEN));

                                                    return 1;
                                                })))))
                .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("identifier", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String identifier = StringArgumentType.getString(context, "identifier");

                                    PlayerPresetManager.removePreset(identifier, isUUID(identifier));

                                    context.getSource().sendFeedback(Text.literal("Removed preset for: " + identifier)
                                            .formatted(Formatting.YELLOW));

                                    return 1;
                                })))
                .then(ClientCommandManager.literal("list")
                        .executes(context -> {
                            var presets = PlayerPresetManager.getAllPresets();
                            if (presets.isEmpty()) {
                                context.getSource().sendFeedback(Text.literal("No presets found.")
                                        .formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.literal("Player Presets:")
                                        .formatted(Formatting.GOLD));
                                for (PlayerPreset preset : presets) {
                                    String displayName = preset.getEffectiveDisplayName();
                                    String identifierType = preset.isUUID() ? "UUID" : "Username";
                                    String line = String.format("- %s (%s: %s) - %.1fx [%s] %s",
                                            displayName,
                                            identifierType,
                                            preset.identifier,
                                            preset.scale,
                                            preset.category,
                                            preset.enabled ? "[Enabled]" : "[Disabled]");
                                    context.getSource().sendFeedback(Text.literal(line)
                                            .formatted(preset.enabled ? Formatting.WHITE : Formatting.GRAY));
                                }
                            }
                            return 1;
                        }))
                .then(ClientCommandManager.literal("toggle")
                        .then(ClientCommandManager.argument("identifier", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String identifier = StringArgumentType.getString(context, "identifier");

                                    // Find the preset
                                    PlayerPreset foundPreset = null;
                                    for (PlayerPreset preset : PlayerPresetManager.getAllPresets()) {
                                        if (preset.identifier.equalsIgnoreCase(identifier)) {
                                            foundPreset = preset;
                                            break;
                                        }
                                    }

                                    if (foundPreset != null) {
                                        // Toggle the enabled state
                                        foundPreset.enabled = !foundPreset.enabled;

                                        // Remove and re-add to save changes
                                        PlayerPresetManager.removePreset(foundPreset.identifier, foundPreset.isUUID());
                                        PlayerPresetManager.addPreset(foundPreset);

                                        String status = foundPreset.enabled ? "enabled" : "disabled";
                                        context.getSource().sendFeedback(Text.literal("Preset for " + identifier + " is now " + status)
                                                .formatted(foundPreset.enabled ? Formatting.GREEN : Formatting.RED));
                                    } else {
                                        context.getSource().sendFeedback(Text.literal("No preset found for: " + identifier)
                                                .formatted(Formatting.RED));
                                    }

                                    return 1;
                                })))
                .then(ClientCommandManager.literal("category")
                        .then(ClientCommandManager.argument("identifier", StringArgumentType.word())
                                .then(ClientCommandManager.argument("category", StringArgumentType.word())
                                        .executes(context -> {
                                            String identifier = StringArgumentType.getString(context, "identifier");
                                            String category = StringArgumentType.getString(context, "category");

                                            // Find the preset
                                            PlayerPreset foundPreset = null;
                                            for (PlayerPreset preset : PlayerPresetManager.getAllPresets()) {
                                                if (preset.identifier.equalsIgnoreCase(identifier)) {
                                                    foundPreset = preset;
                                                    break;
                                                }
                                            }

                                            if (foundPreset != null) {
                                                // Update category
                                                foundPreset.category = category;

                                                // Remove and re-add to save changes
                                                PlayerPresetManager.removePreset(foundPreset.identifier, foundPreset.isUUID());
                                                PlayerPresetManager.addPreset(foundPreset);

                                                context.getSource().sendFeedback(Text.literal("Updated category for " + identifier + " to: " + category)
                                                        .formatted(Formatting.GREEN));
                                            } else {
                                                context.getSource().sendFeedback(Text.literal("No preset found for: " + identifier)
                                                        .formatted(Formatting.RED));
                                            }

                                            return 1;
                                        }))))
                .then(ClientCommandManager.literal("help")
                        .executes(context -> {
                            context.getSource().sendFeedback(Text.literal("ScaleMe Preset Commands:")
                                    .formatted(Formatting.GOLD));
                            context.getSource().sendFeedback(Text.literal("/scalepreset add <identifier> <scale> [displayName] - Add a new preset"));
                            context.getSource().sendFeedback(Text.literal("/scalepreset remove <identifier> - Remove a preset"));
                            context.getSource().sendFeedback(Text.literal("/scalepreset list - List all presets"));
                            context.getSource().sendFeedback(Text.literal("/scalepreset toggle <identifier> - Enable/disable a preset"));
                            context.getSource().sendFeedback(Text.literal("/scalepreset category <identifier> <category> - Change preset category"));
                            context.getSource().sendFeedback(Text.literal("/scalepreset help - Show this help"));
                            context.getSource().sendFeedback(Text.literal("Scale range: 0.1x to 3.0x")
                                    .formatted(Formatting.GRAY));
                            context.getSource().sendFeedback(Text.literal("UUIDs are automatically detected")
                                    .formatted(Formatting.GRAY));
                            return 1;
                        }))
        );
    }

    private static boolean isUUID(String identifier) {
        // Simple UUID detection - 36 characters with hyphens in correct positions
        return identifier != null && identifier.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }
}