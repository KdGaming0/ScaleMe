package com.github.scaleme.client.command;

import com.github.scaleme.client.util.HypixelDetector;
import com.github.scaleme.client.util.ScaleManager;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HypixelStatusCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("scaleme")
                .then(ClientCommandManager.literal("hypixel")
                        .executes(context -> {
                            // Update detection before showing status
                            HypixelDetector.updateDetection();

                            boolean onHypixel = HypixelDetector.isOnHypixel();
                            boolean inCompetitive = HypixelDetector.isInCompetitiveGame();
                            boolean scalingAllowed = HypixelDetector.isScalingAllowed();
                            String gameMode = HypixelDetector.getCurrentGameMode();

                            context.getSource().sendFeedback(Text.literal("=== ScaleMe Hypixel Status ===")
                                    .formatted(Formatting.GOLD));

                            context.getSource().sendFeedback(Text.literal("On Hypixel: " + (onHypixel ? "Yes" : "No"))
                                    .formatted(onHypixel ? Formatting.GREEN : Formatting.RED));

                            if (onHypixel) {
                                context.getSource().sendFeedback(Text.literal("In Competitive Game: " + (inCompetitive ? "Yes" : "No"))
                                        .formatted(inCompetitive ? Formatting.RED : Formatting.GREEN));

                                if (inCompetitive && !gameMode.isEmpty()) {
                                    context.getSource().sendFeedback(Text.literal("Current Game: " + gameMode)
                                            .formatted(Formatting.YELLOW));
                                }
                            }

                            context.getSource().sendFeedback(Text.literal("Scaling Allowed: " + (scalingAllowed ? "Yes" : "No"))
                                    .formatted(scalingAllowed ? Formatting.GREEN : Formatting.RED));

                            if (!scalingAllowed) {
                                String reason = ScaleManager.getRestrictionReason();
                                if (!reason.isEmpty()) {
                                    context.getSource().sendFeedback(Text.literal("Reason: " + reason)
                                            .formatted(Formatting.GRAY));
                                }
                            }

                            return 1;
                        })));
    }
}