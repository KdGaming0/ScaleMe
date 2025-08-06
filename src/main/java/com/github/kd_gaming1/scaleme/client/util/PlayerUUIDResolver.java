package com.github.kd_gaming1.scaleme.client.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerUUIDResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerUUIDResolver.class);
    private static final ConcurrentHashMap<String, UUID> uuidCache = new ConcurrentHashMap<>();

    public static UUID resolvePlayerUUID(String playerName) {
        if (playerName == null || playerName.isBlank()) return null;
        String trimmed = playerName.trim().toLowerCase();

        return uuidCache.computeIfAbsent(trimmed, PlayerUUIDResolver::resolveUUIDInternal);
    }

    private static UUID resolveUUIDInternal(String trimmed) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if it's the current player
        if (client.player != null && trimmed.equalsIgnoreCase(client.player.getName().getString())) {
            return client.player.getUuid();
        }

        // Check player list cache
        if (client.getNetworkHandler() != null) {
            UUID foundUUID = client.getNetworkHandler().getPlayerList().stream()
                    .filter(entry -> entry.getProfile().getName().equalsIgnoreCase(trimmed))
                    .map(entry -> entry.getProfile().getId())
                    .findFirst()
                    .orElse(null);
            if (foundUUID != null) return foundUUID;
        }

        // Try to parse as UUID
        try {
            return UUID.fromString(trimmed);
        } catch (IllegalArgumentException ignored) {}

        // Fallback: Query Mojang API
        return fetchUUIDFromMojang(trimmed);
    }

    private static UUID fetchUUIDFromMojang(String playerName) {
        try {
            String urlStr = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
            HttpURLConnection conn = (HttpURLConnection) java.net.URI.create(urlStr).toURL().openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 204) {
                LOGGER.info("Mojang API: Player '{}' not found.", playerName);
                return null;
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);

                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                String id = json.get("id").getAsString();
                String uuidStr = id.replaceFirst(
                        "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                        "$1-$2-$3-$4-$5"
                );
                return UUID.fromString(uuidStr);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to resolve UUID for '{}': {}", playerName, e.getMessage());
            return null;
        }
    }
}