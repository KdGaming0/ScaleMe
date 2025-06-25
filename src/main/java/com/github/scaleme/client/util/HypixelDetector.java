package com.github.scaleme.client.util;

import com.github.scaleme.Scaleme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.text.Text;

import java.util.Set;

/**
 * Utility class for detecting Hypixel server and competitive game modes
 * to ensure the mod complies with Hypixel's rules and prevents unfair advantages
 */
public class HypixelDetector {

    // Hypixel server address patterns
    private static final Set<String> HYPIXEL_DOMAINS = Set.of(
            "hypixel.net",
            "mc.hypixel.net",
            "alpha.hypixel.net"
    );

    // Competitive game modes where scaling could provide unfair advantages
    private static final Set<String> COMPETITIVE_GAMES = Set.of(
            // PvP Games
            "bed wars", "bedwars", "bw",
            "sky wars", "skywars", "sw",
            "the bridge", "bridge", "duels",
            "mega walls", "megawalls", "mw",
            "crazy walls", "crazywalls", "cw",
            "uhc champions", "uhc", "champions",
            "smash heroes", "smash", "sh",
            "blitz survival games", "blitz", "bsg",
            "tnt games", "tnt", "bow spleef", "tnt tag", "tnt run",
            "arena brawl", "arena",
            "warlords",
            "murder mystery", "mm",
            "cops and crims", "cvc",

            // Competitive Minigames
            "build battle", "bb",
            "speed uhc", "speed",
            "prototype",
            "tournament",
            "ranked",
            "guild",

            // Hide and Seek (scaling could be unfair)
            "hide and seek", "party games", "mini walls"
    );

    // Skyblock-specific areas that are safe
    private static final Set<String> SKYBLOCK_SAFE_AREAS = Set.of(
            "private island", "garden", "hub", "barn", "mushroom desert",
            "gold mine", "deep caverns", "dwarven mines", "crystal hollows",
            "farming islands", "the end", "crimson isle", "jerry's workshop",
            "winter island", "rift", "kuudra", "instanced area"
    );

    private static boolean isOnHypixel = false;
    private static boolean isInCompetitiveGame = false;
    private static String currentGameMode = "";
    private static String currentLocation = "";
    private static long lastCheck = 0;
    private static final long CHECK_INTERVAL = 1000; // Check every second

    /**
     * Updates the Hypixel detection status
     * Should be called regularly from the client tick
     */
    public static void updateDetection() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheck < CHECK_INTERVAL) {
            return;
        }
        lastCheck = currentTime;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            reset();
            return;
        }

        // Check if we're on Hypixel
        boolean wasOnHypixel = isOnHypixel;
        isOnHypixel = detectHypixelServer();

        if (!wasOnHypixel && isOnHypixel) {
            Scaleme.LOGGER.info("Detected Hypixel server - enabling game mode detection");
        } else if (wasOnHypixel && !isOnHypixel) {
            Scaleme.LOGGER.info("Left Hypixel server - disabling restrictions");
        }

        if (isOnHypixel) {
            detectGameMode();
        } else {
            isInCompetitiveGame = false;
            currentGameMode = "";
            currentLocation = "";
        }
    }

    /**
     * Detects if the client is connected to a Hypixel server
     */
    private static boolean detectHypixelServer() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check server info
        ServerInfo serverInfo = client.getCurrentServerEntry();
        if (serverInfo != null) {
            String address = serverInfo.address.toLowerCase();
            for (String domain : HYPIXEL_DOMAINS) {
                if (address.contains(domain)) {
                    return true;
                }
            }
        }

        // Check integrated server (shouldn't be Hypixel, but just in case)
        if (client.getServer() != null) {
            return false;
        }

        return false;
    }

    /**
     * Detects the current game mode and location on Hypixel
     */
    private static void detectGameMode() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Check scoreboard for game information
        String gameInfo = getScoreboardGameInfo();
        String tabListInfo = getTabListGameInfo();
        String locationInfo = getLocationInfo();

        // Combine all sources of information
        String combinedInfo = (gameInfo + " " + tabListInfo + " " + locationInfo).toLowerCase();

        // Update current location and game mode
        currentLocation = locationInfo.toLowerCase();
        currentGameMode = extractGameMode(combinedInfo);

        // Determine if we're in a competitive game
        boolean wasInCompetitive = isInCompetitiveGame;
        isInCompetitiveGame = isCompetitiveMode(combinedInfo, currentLocation);

        // Log game mode changes
        if (!wasInCompetitive && isInCompetitiveGame) {
            Scaleme.LOGGER.warn("Entered competitive game mode: {} - ScaleMe scaling disabled", currentGameMode);
        } else if (wasInCompetitive && !isInCompetitiveGame) {
            Scaleme.LOGGER.info("Left competitive game mode - ScaleMe scaling re-enabled");
        }
    }

    /**
     * Extracts game information from scoreboard
     */
    private static String getScoreboardGameInfo() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.world.getScoreboard() == null) {
            return "";
        }

        try {
            var scoreboard = client.world.getScoreboard();
            // Use the correct ScoreboardDisplaySlot enum instead of int
            var objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
            if (objective != null) {
                Text displayName = objective.getDisplayName();
                if (displayName != null) {
                    return displayName.getString();
                }
            }
        } catch (Exception e) {
            // Ignore scoreboard reading errors
        }

        return "";
    }

    /**
     * Extracts game information from tab list
     */
    private static String getTabListGameInfo() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) {
            return "";
        }

        try {
            var playerListHud = client.inGameHud.getPlayerListHud();
            // Fixed: Use the correct getter methods
            Text header = getTabListHeader(playerListHud);
            Text footer = getTabListFooter(playerListHud);

            String headerText = header != null ? header.getString() : "";
            String footerText = footer != null ? footer.getString() : "";

            return headerText + " " + footerText;
        } catch (Exception e) {
            // Ignore tab list reading errors
        }

        return "";
    }

    /**
     * Helper method to safely get tab list header using reflection as fallback
     */
    private static Text getTabListHeader(net.minecraft.client.gui.hud.PlayerListHud playerListHud) {
        try {
            // Try to use reflection to access the header field
            var headerField = playerListHud.getClass().getDeclaredField("header");
            headerField.setAccessible(true);
            return (Text) headerField.get(playerListHud);
        } catch (Exception e) {
            // If reflection fails, return null
            return null;
        }
    }

    /**
     * Helper method to safely get tab list footer using reflection as fallback
     */
    private static Text getTabListFooter(net.minecraft.client.gui.hud.PlayerListHud playerListHud) {
        try {
            // Try to use reflection to access the footer field
            var footerField = playerListHud.getClass().getDeclaredField("footer");
            footerField.setAccessible(true);
            return (Text) footerField.get(playerListHud);
        } catch (Exception e) {
            // If reflection fails, return null
            return null;
        }
    }

    /**
     * Gets location information from various sources
     */
    private static String getLocationInfo() {
        // This could be enhanced to read from action bar, chat messages, etc.
        // For now, we'll rely on scoreboard and tab list
        return "";
    }

    /**
     * Extracts the current game mode from combined information
     */
    private static String extractGameMode(String combinedInfo) {
        for (String game : COMPETITIVE_GAMES) {
            if (combinedInfo.contains(game)) {
                return game;
            }
        }
        return "unknown";
    }

    /**
     * Determines if the current mode is competitive
     */
    private static boolean isCompetitiveMode(String combinedInfo, String location) {
        // Always allow in Skyblock safe areas
        for (String safeArea : SKYBLOCK_SAFE_AREAS) {
            if (location.contains(safeArea) || combinedInfo.contains(safeArea)) {
                return false;
            }
        }

        // Check for competitive games
        for (String competitiveGame : COMPETITIVE_GAMES) {
            if (combinedInfo.contains(competitiveGame)) {
                return true;
            }
        }

        // If we detect "skyblock" but not in a safe area, be cautious
        if (combinedInfo.contains("skyblock") && !isSkyblockSafeArea(combinedInfo)) {
            // Allow scaling in most Skyblock areas, but be careful about dungeons/PvP areas
            return combinedInfo.contains("dungeon") ||
                    combinedInfo.contains("catacombs") ||
                    combinedInfo.contains("master mode") ||
                    combinedInfo.contains("floor");
        }

        return false;
    }

    /**
     * Checks if we're in a safe Skyblock area
     */
    private static boolean isSkyblockSafeArea(String info) {
        for (String safeArea : SKYBLOCK_SAFE_AREAS) {
            if (info.contains(safeArea)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resets all detection state
     */
    private static void reset() {
        isOnHypixel = false;
        isInCompetitiveGame = false;
        currentGameMode = "";
        currentLocation = "";
    }

    // Public getters
    public static boolean isOnHypixel() {
        return isOnHypixel;
    }

    public static boolean isInCompetitiveGame() {
        return isInCompetitiveGame;
    }

    public static boolean isScalingAllowed() {
        return !isOnHypixel || !isInCompetitiveGame;
    }

    public static String getCurrentGameMode() {
        return currentGameMode;
    }

    public static String getCurrentLocation() {
        return currentLocation;
    }
}