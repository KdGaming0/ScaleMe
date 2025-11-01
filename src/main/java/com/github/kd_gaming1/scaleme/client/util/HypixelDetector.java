package com.github.kd_gaming1.scaleme.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.*;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Utility for detecting Hypixel server contexts and game modes.
 * <p>
 * Provides safety checks to prevent scaling in competitive or gameplay-sensitive areas.
 */
public class HypixelDetector {

    // Server brand detection
    private static final String HYPIXEL_BRAND = "hypixel";

    // Unsafe game mode prefixes (where scaling should be disabled)
    private static final String[] UNSAFE_MODE_PREFIXES = {
            "SKYBLOCK",
            "LOBBY",
            "LIMBO",
            "HOUSING",
            "PROTOTYPE",
            "HYPIXEL"
    };

    // Dungeon detection marker
    private static final String DUNGEON_MARKER_PREFIX = "CLEARED:";

    // Color code pattern for stripping
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§.");

    // Non-breaking space character
    private static final char NBSP = '\u00A0';

    /**
     * Checks if the player is in a "safe" game mode on Hypixel.
     * <p>
     * Safe modes exclude competitive/gameplay areas like:
     * <ul>
     *   <li>SkyBlock</li>
     *   <li>Lobby</li>
     *   <li>Limbo</li>
     *   <li>Housing</li>
     *   <li>Prototype</li>
     *   <li>Hypixel logo screen</li>
     * </ul>
     *
     * @return true if in a safe game mode, false otherwise
     */
    public static boolean isSafeGameMode() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Must be connected to a server
        if (!isConnectedToServer(client)) {
            return false;
        }

        // Must be on Hypixel
        if (!isHypixelServer(client)) {
            return false;
        }

        // Check scoreboard for game mode
        String gameMode = getCurrentGameMode(client);
        if (gameMode == null) {
            return false;
        }

        // Return true if NOT in any unsafe game mode
        return !isUnsafeGameMode(gameMode);
    }

    /**
     * Checks if NPC scaling should be disabled for gameplay safety.
     * <p>
     * NPC scaling is disabled when:
     * <ul>
     *   <li>Not in a safe game mode (e.g., in SkyBlock, Lobby, Housing)</li>
     *   <li>AND inside a dungeon</li>
     * </ul>
     * <p>
     * This prevents NPC scaling from affecting competitive gameplay in dungeons.
     * Note: The method name is misleading (kept for compatibility). It returns true
     * when scaling should be DISABLED, not when it's safe to scale.
     *
     * @return true if NPC scaling should be disabled (in dungeons)
     */
    public static boolean isSafeForNPCScaling() {
        return !isSafeGameMode() && isInDungeon();
    }

    /**
     * Detects if the player is currently inside a Hypixel dungeon.
     * <p>
     * Detection is done by scanning the scoreboard for a line starting with "CLEARED:".
     *
     * @return true if in a dungeon, false otherwise
     */
    public static boolean isInDungeon() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) {
            return false;
        }

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);

        if (objective == null) {
            return false;
        }

        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);

        for (ScoreboardEntry entry : entries) {
            String line = buildScoreboardLine(entry, scoreboard);
            String normalized = normalizeScoreboardLine(line);

            if (isDungeonLine(normalized)) {
                return true;
            }
        }

        return false;
    }

    // ===== Private Helper Methods =====

    /**
     * Checks if the client is connected to a server.
     */
    private static boolean isConnectedToServer(MinecraftClient client) {
        return client.getNetworkHandler() != null;
    }

    /**
     * Checks if the connected server is Hypixel.
     */
    private static boolean isHypixelServer(MinecraftClient client) {
        String brand = client.getNetworkHandler().getBrand();
        return brand != null && brand.toLowerCase().contains(HYPIXEL_BRAND);
    }

    /**
     * Gets the current game mode from the scoreboard title.
     *
     * @return The game mode title (uppercase, color codes stripped), or null if unavailable
     */
    private static String getCurrentGameMode(MinecraftClient client) {
        if (client.world == null) {
            return null;
        }

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);

        if (objective == null) {
            return null;
        }

        String title = objective.getDisplayName().getString();
        return stripColors(title).toUpperCase();
    }

    /**
     * Checks if a game mode is considered unsafe for scaling.
     */
    private static boolean isUnsafeGameMode(String gameMode) {
        for (String unsafePrefix : UNSAFE_MODE_PREFIXES) {
            if (gameMode.startsWith(unsafePrefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a complete scoreboard line including team prefix/suffix.
     */
    private static String buildScoreboardLine(ScoreboardEntry entry, Scoreboard scoreboard) {
        String owner = entry.owner();
        Team team = scoreboard.getScoreHolderTeam(owner);

        StringBuilder lineBuilder = new StringBuilder();

        if (team != null) {
            lineBuilder.append(team.getPrefix().getString());
        }

        lineBuilder.append(owner);

        if (team != null) {
            lineBuilder.append(team.getSuffix().getString());
        }

        return lineBuilder.toString();
    }

    /**
     * Normalizes a scoreboard line for comparison.
     * <p>
     * Normalization includes:
     * <ul>
     *   <li>Strip color codes</li>
     *   <li>Replace non-breaking spaces</li>
     *   <li>Collapse whitespace</li>
     *   <li>Trim and uppercase</li>
     * </ul>
     */
    private static String normalizeScoreboardLine(String line) {
        try {
            String noColors = stripColors(line);
            return noColors
                    .replace(NBSP, ' ')
                    .replaceAll("\\s+", " ")
                    .trim()
                    .toUpperCase();
        } catch (Exception e) {
            // Fallback to basic normalization if something goes wrong
            return line.toUpperCase().trim();
        }
    }

    /**
     * Checks if a normalized line indicates a dungeon.
     */
    private static boolean isDungeonLine(String normalizedLine) {
        return normalizedLine.startsWith(DUNGEON_MARKER_PREFIX) ||
                normalizedLine.contains(DUNGEON_MARKER_PREFIX);
    }

    /**
     * Strips Minecraft formatting codes (§ followed by any character).
     *
     * @param text The text to strip
     * @return Text with all color codes removed
     */
    private static String stripColors(String text) {
        if (text == null) {
            return "";
        }
        return COLOR_CODE_PATTERN.matcher(text).replaceAll("");
    }
}