package com.github.kd_gaming1.scaleme.client.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves Minecraft player UUIDs from usernames.
 * <p>
 * Resolution priority:
 * <ol>
 *   <li>Cache lookup</li>
 *   <li>Current player check</li>
 *   <li>Player list check (online players)</li>
 *   <li>Direct UUID parsing (if input is already a UUID)</li>
 *   <li>Mojang API query (fallback)</li>
 * </ol>
 * <p>
 * Thread-safe with caching to minimize API calls.
 */
public class PlayerUUIDResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerUUIDResolver.class);

    // Mojang API configuration
    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final int HTTP_NOT_FOUND = 204;

    // UUID format pattern
    private static final String UUID_INSERTION_PATTERN = "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})";
    private static final String UUID_FORMAT = "$1-$2-$3-$4-$5";

    // Cache for resolved UUIDs
    private static final ConcurrentHashMap<String, UUID> uuidCache = new ConcurrentHashMap<>();

    /**
     * Resolves a player's UUID from their username.
     * <p>
     * The input can be:
     * <ul>
     *   <li>A player username (e.g., "Notch")</li>
     *   <li>A UUID string (with or without hyphens)</li>
     * </ul>
     *
     * @param playerName The player name or UUID string
     * @return The resolved UUID, or null if resolution fails
     */
    public static UUID resolvePlayerUUID(String playerName) {
        if (isInvalidInput(playerName)) {
            return null;
        }

        String normalizedName = normalizeName(playerName);
        return uuidCache.computeIfAbsent(normalizedName, PlayerUUIDResolver::resolveUUIDInternal);
    }

    /**
     * Clears the UUID cache.
     * Useful for testing or when player data changes.
     */
    public static void clearCache() {
        uuidCache.clear();
        LOGGER.debug("UUID cache cleared");
    }

    /**
     * Gets the current cache size.
     *
     * @return Number of cached UUID entries
     */
    public static int getCacheSize() {
        return uuidCache.size();
    }

    // ===== Private Helper Methods =====

    /**
     * Internal resolution method that tries all available sources.
     */
    private static UUID resolveUUIDInternal(String normalizedName) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Priority 1: Check if it's the current player
        UUID ownPlayerUUID = checkCurrentPlayer(normalizedName, client);
        if (ownPlayerUUID != null) {
            return ownPlayerUUID;
        }

        // Priority 2: Check player list (online players)
        UUID playerListUUID = checkPlayerList(normalizedName, client);
        if (playerListUUID != null) {
            return playerListUUID;
        }

        // Priority 3: Try parsing as UUID
        UUID parsedUUID = tryParseUUID(normalizedName);
        if (parsedUUID != null) {
            return parsedUUID;
        }

        // Priority 4: Query Mojang API (fallback)
        return queryMojangAPI(normalizedName);
    }

    /**
     * Checks if the name matches the current player.
     */
    private static UUID checkCurrentPlayer(String normalizedName, MinecraftClient client) {
        if (client.player == null) {
            return null;
        }

        String playerName = client.player.getName().getString().toLowerCase();
        if (normalizedName.equals(playerName)) {
            return client.player.getUuid();
        }

        return null;
    }

    /**
     * Checks the player list for online players.
     */
    private static UUID checkPlayerList(String normalizedName, MinecraftClient client) {
        if (client.getNetworkHandler() == null) {
            return null;
        }

        return client.getNetworkHandler().getPlayerList().stream()
                .filter(entry -> entry.getProfile().getName().equalsIgnoreCase(normalizedName))
                .map(entry -> entry.getProfile().getId())
                .findFirst()
                .orElse(null);
    }

    /**
     * Attempts to parse the input as a UUID string.
     */
    private static UUID tryParseUUID(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            // Not a valid UUID format
            return null;
        }
    }

    /**
     * Queries the Mojang API to resolve a username to UUID.
     */
    private static UUID queryMojangAPI(String playerName) {
        try {
            String apiUrl = MOJANG_API_URL + playerName;
            HttpURLConnection connection = createConnection(apiUrl);

            // Check for "not found" response
            if (connection.getResponseCode() == HTTP_NOT_FOUND) {
                LOGGER.info("Player '{}' not found in Mojang database", playerName);
                return null;
            }

            String responseJson = readResponse(connection);
            return parseUUIDFromJson(responseJson, playerName);

        } catch (Exception e) {
            LOGGER.error("Failed to resolve UUID for '{}': {}", playerName, e.getMessage());
            return null;
        }
    }

    /**
     * Creates an HTTP connection to the Mojang API.
     */
    private static HttpURLConnection createConnection(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)
                java.net.URI.create(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection;
    }

    /**
     * Reads the response from an HTTP connection.
     */
    private static String readResponse(HttpURLConnection connection) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        }
    }

    /**
     * Parses a UUID from the Mojang API JSON response.
     */
    private static UUID parseUUIDFromJson(String jsonResponse, String playerName) {
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String uuidWithoutHyphens = json.get("id").getAsString();
            String formattedUUID = formatUUID(uuidWithoutHyphens);

            return UUID.fromString(formattedUUID);
        } catch (Exception e) {
            LOGGER.error("Failed to parse UUID from Mojang response for '{}': {}",
                    playerName, e.getMessage());
            return null;
        }
    }

    /**
     * Formats a UUID string without hyphens to standard format with hyphens.
     *
     * @param uuidWithoutHyphens UUID string without hyphens (32 characters)
     * @return Formatted UUID string with hyphens
     */
    private static String formatUUID(String uuidWithoutHyphens) {
        return uuidWithoutHyphens.replaceFirst(UUID_INSERTION_PATTERN, UUID_FORMAT);
    }

    /**
     * Validates input and checks if it's usable.
     */
    private static boolean isInvalidInput(String input) {
        return input == null || input.isBlank();
    }

    /**
     * Normalizes a player name for cache key.
     */
    private static String normalizeName(String playerName) {
        return playerName.trim().toLowerCase();
    }
}