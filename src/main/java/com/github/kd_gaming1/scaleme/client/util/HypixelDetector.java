package com.github.kd_gaming1.scaleme.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.*;

import java.util.Collection;

public class HypixelDetector {

    /**
     * Checks if the player is in a "safe" game mode on Hypixel.
     * Safe modes exclude competitive/gameplay areas like SkyBlock, Lobby, Limbo, Housing, Prototype, and the Hypixel logo screen.
     *
     * @return true if in a safe game mode, false otherwise.
     */
    public static boolean isSafeGameMode() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Not connected to a server
        if (client.getNetworkHandler() == null) return false;

        // Must be Hypixel
        String brand = client.getNetworkHandler().getBrand();
        if (brand == null || !brand.toLowerCase().contains("hypixel")) return false;

        if (client.world == null) return false;

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;

        String title = stripColors(objective.getDisplayName().getString()).toUpperCase();

        // Return true if NOT in any of these game modes
        return !title.startsWith("SKYBLOCK") &&
                !title.startsWith("LOBBY") &&
                !title.startsWith("LIMBO") &&
                !title.startsWith("HOUSING") &&
                !title.startsWith("PROTOTYPE") &&
                !title.startsWith("HYPIXEL");
    }

    /**
     * Checks if the current mode is safe for NPC scaling.
     * This means not being in a safe game mode and being inside a dungeon.
     */
    public static boolean isSafeForNPCScaling() {
        return !isSafeGameMode() && isInDungeon();
    }

    /**
     * Detects if the player is currently inside a Hypixel dungeon by scanning the scoreboard.
     * Looks for a line starting with "CLEARED:".
     */
    public static boolean isInDungeon() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;

        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);

        for (ScoreboardEntry entry : entries) {
            String owner = entry.owner();
            Team team = scoreboard.getScoreHolderTeam(owner);

            // Build the full line with prefix/suffix
            StringBuilder sb = new StringBuilder();
            if (team != null) sb.append(team.getPrefix().getString());
            sb.append(owner);
            if (team != null) sb.append(team.getSuffix().getString());

            String rawLine = sb.toString();

            // Strip colors, normalize spaces, uppercase
            String noColors;
            try {
                noColors = stripColors(rawLine);
            } catch (Throwable t) {
                noColors = rawLine.replaceAll("(?i)\\u00A7[0-9A-FK-OR]", "");
            }

            String line = noColors
                    .replace('\u00A0', ' ')  // NBSP -> space
                    .replaceAll("\\s+", " ") // collapse whitespace
                    .trim()
                    .toUpperCase();

            // Primary and fallback check
            if (line.startsWith("CLEARED:") || line.contains("CLEARED:")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Strips Minecraft formatting codes (§ followed by any character).
     */
    private static String stripColors(String text) {
        return text.replaceAll("§.", "");
    }
}