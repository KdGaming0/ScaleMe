package com.github.scaleme.client.util;

import com.github.scaleme.client.data.PlayerPreset;
import com.github.scaleme.config.ScaleMeConfig;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScaleManager {
    private static float currentOwnScale = 1.0f;
    private static float targetOwnScale = 1.0f;
    private static float currentOtherScale = 1.0f;
    private static float targetOtherScale = 1.0f;
    private static long lastUpdateTime = 0;

    // Cache for player-specific scales
    private static final ConcurrentHashMap<UUID, Float> currentPlayerScales = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Float> targetPlayerScales = new ConcurrentHashMap<>();

    public static void init() {
        currentOwnScale = ScaleMeConfig.ownPlayerScale;
        targetOwnScale = ScaleMeConfig.ownPlayerScale;
        currentOtherScale = ScaleMeConfig.otherPlayersScale;
        targetOtherScale = ScaleMeConfig.otherPlayersScale;

        // Initialize player preset manager
        PlayerPresetManager.init();
    }

    public static void tick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < 16) return; // ~60 FPS limit
        lastUpdateTime = currentTime;

        // Update target scales from config
        targetOwnScale = ScaleMeConfig.ownPlayerScale;
        targetOtherScale = ScaleMeConfig.otherPlayersScale;

        // Update own player scale
        if (ScaleMeConfig.ownPlayerSmoothScaling) {
            float ownDifference = targetOwnScale - currentOwnScale;
            if (Math.abs(ownDifference) > 0.001f) {
                currentOwnScale += ownDifference * 0.1f;
            } else {
                currentOwnScale = targetOwnScale;
            }
        } else {
            currentOwnScale = targetOwnScale;
        }

        // Update other players scale
        if (ScaleMeConfig.otherPlayersSmoothScaling) {
            float otherDifference = targetOtherScale - currentOtherScale;
            if (Math.abs(otherDifference) > 0.001f) {
                currentOtherScale += otherDifference * 0.1f;
            } else {
                currentOtherScale = targetOtherScale;
            }
        } else {
            currentOtherScale = targetOtherScale;
        }

        // Update individual player scales with smooth scaling
        for (UUID uuid : currentPlayerScales.keySet()) {
            float current = currentPlayerScales.get(uuid);
            float target = targetPlayerScales.getOrDefault(uuid, 1.0f);

            if (ScaleMeConfig.otherPlayersSmoothScaling) {
                float difference = target - current;
                if (Math.abs(difference) > 0.001f) {
                    currentPlayerScales.put(uuid, current + difference * 0.1f);
                } else {
                    currentPlayerScales.put(uuid, target);
                }
            } else {
                currentPlayerScales.put(uuid, target);
            }
        }
    }

    public static float getCurrentScale(UUID playerUUID) {
        tick();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || playerUUID == null) {
            return 1.0f;
        }

        boolean isOwnPlayer = playerUUID.equals(client.player.getUuid());

        // Check for player-specific presets first (highest priority)
        if (ScaleMeConfig.enablePlayerPresets && !isOwnPlayer) {
            PlayerPreset preset = PlayerPresetManager.getPresetForPlayer(playerUUID);
            if (preset != null) {
                // Update target scale for this player
                targetPlayerScales.put(playerUUID, preset.scale);
                // Return current scale (for smooth scaling)
                return currentPlayerScales.computeIfAbsent(playerUUID, k -> preset.scale);
            }
        }

        // If "Apply to All Players" is enabled, use other players scale for everyone
        if (ScaleMeConfig.applyToAllPlayers && ScaleMeConfig.enableOtherPlayersScaling) {
            return currentOtherScale;
        }

        // If it's the own player, use own player scale
        if (isOwnPlayer) {
            return currentOwnScale;
        }

        // If it's another player and other players scaling is enabled, use other players scale
        if (ScaleMeConfig.enableOtherPlayersScaling) {
            return currentOtherScale;
        }

        // Default: no scaling
        return 1.0f;
    }

    public static void reloadPresets() {
        PlayerPresetManager.loadPresets();
        // Clear current player scales to force recalculation
        currentPlayerScales.clear();
        targetPlayerScales.clear();
    }
}