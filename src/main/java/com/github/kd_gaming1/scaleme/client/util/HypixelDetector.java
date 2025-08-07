package com.github.kd_gaming1.scaleme.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.text.Text;

public class HypixelDetector {

    public static String currentMode = null;
    public static String currentMap = null;
    public static String currentServerName = null;
    public static String serverType = null;

    public static boolean isSafeGameMode() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if on Hypixel using server brand
        if (client.getNetworkHandler() == null) return false; // Not on a server, safe
        String brand = client.getNetworkHandler().getBrand();
        if (brand == null || !brand.toLowerCase().contains("hypixel")) return false; // Not Hypixel, safe

        // Get scoreboard title
        if (client.world == null) return false; // Safe default for Hypixel
        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) return false; // Safe default for Hypixel

        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false; // Safe default for Hypixel

        Text scoreboardTitle = objective.getDisplayName();
        String title = stripColors(scoreboardTitle.getString()).toUpperCase();

        // Return true if in safe modes
        return !title.startsWith("SKYBLOCK") &&
                !title.startsWith("LOBBY") &&
                !title.startsWith("LIMBO") &&
                !title.startsWith("HOUSING") &&
                !title.startsWith("PROTOTYPE");
    }

    /**
     * Checks if the player is in a Hypixel dungeon or catacombs.
     * This method uses the current mode, map, and server name to determine if the player is in dungeons.
     *
     * @return true if the player is in dungeons, false otherwise.
     */
    public static boolean isInDungeons() {
        if (!"SKYBLOCK".equalsIgnoreCase(serverType)) return true;

        String mode = currentMode != null ? currentMode.toLowerCase() : "";
        String map = currentMap != null ? currentMap.toLowerCase() : "";
        String server = currentServerName != null ? currentServerName.toLowerCase() : "";

        return !mode.contains("dungeon") && !mode.contains("catacombs")
                && !map.contains("dungeon") && !map.contains("catacombs") && !map.contains("floor")
                && !server.contains("dungeon") && !server.contains("catacombs");
    }

    private static String stripColors(String text) {
        // Remove Minecraft color codes (§ followed by any character)
        return text.replaceAll("§.", "");
    }
}