package com.github.kd_gaming1.scaleme.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.text.Text;

public class HypixelDetector {

    public static boolean isSafeGameMode() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if on Hypixel using server brand
        if (client.getNetworkHandler() == null) return true; // Not on a server, safe
        String brand = client.getNetworkHandler().getBrand();
        if (brand == null || !brand.toLowerCase().contains("hypixel")) return true; // Not Hypixel, safe

        // Get scoreboard title
        if (client.world == null) return true; // Safe default for Hypixel
        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) return true; // Safe default for Hypixel

        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return true; // Safe default for Hypixel

        Text scoreboardTitle = objective.getDisplayName();
        String title = stripColors(scoreboardTitle.getString()).toUpperCase();

        // Return true if in safe modes
        return title.startsWith("SKYBLOCK") ||
                title.startsWith("LOBBY") ||
                title.startsWith("LIMBO") ||
                title.startsWith("HOUSING") ||
                title.startsWith("PROTOTYPE");
    }

    private static String stripColors(String text) {
        // Remove Minecraft color codes (§ followed by any character)
        return text.replaceAll("§.", "");
    }
}