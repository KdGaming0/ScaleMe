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

        // Update Hypixel detection if safety mode is enabled
        if (ScaleMeConfig.enableHypixelSafety) {
            HypixelDetector.updateDetection();
        }

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

        // HYPIXEL SAFETY CHECK: Disable scaling in competitive games if safety mode is enabled
        if (ScaleMeConfig.enableHypixelSafety && !HypixelDetector.isScalingAllowed()) {
            return 1.0f; // Return normal scale when scaling is not allowed
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || playerUUID == null) {
            return 1.0f;
        }

        boolean isOwnPlayer = playerUUID.equals(client.player.getUuid());

        // Check for player-specific presets first (highest priority)
        if (ScaleMeConfig.enablePlayerPresets && !isOwnPlayer) {
            PlayerPreset preset = PlayerPresetManager.getPresetForPlayer(playerUUID);
            if (preset != null) {
                // Cache the target scale for smooth scaling
                targetPlayerScales.put(playerUUID, preset.scale);
                return currentPlayerScales.computeIfAbsent(playerUUID, k -> preset.scale);
            }
        }

        // Use global scales if no preset found
        if (isOwnPlayer) {
            return currentOwnScale;
        } else {
            // For other players, use global scale if no preset exists
            targetPlayerScales.put(playerUUID, currentOtherScale);
            return currentPlayerScales.computeIfAbsent(playerUUID, k -> currentOtherScale);
        }
    }

    // Add method to check if scaling is currently active
    public static boolean isScalingActive() {
        if (!ScaleMeConfig.enableHypixelSafety) {
            return true; // If safety mode is disabled, scaling is always active
        }
        return HypixelDetector.isScalingAllowed();
    }

    // Add method to get restriction reason
    public static String getRestrictionReason() {
        if (ScaleMeConfig.enableHypixelSafety && HypixelDetector.isOnHypixel() && HypixelDetector.isInCompetitiveGame()) {
            return "Disabled in competitive game: " + HypixelDetector.getCurrentGameMode();
        }
        return "";
    }

    // Existing methods...
    public static void setOwnPlayerScale(float scale) {
        targetOwnScale = Math.max(0.1f, Math.min(3.0f, scale));
    }

    public static void setOtherPlayersScale(float scale) {
        targetOtherScale = Math.max(0.1f, Math.min(3.0f, scale));
    }

    public static float getOwnPlayerScale() {
        return currentOwnScale;
    }

    public static float getOtherPlayersScale() {
        return currentOtherScale;
    }

    public static void clearPlayerScale(UUID playerUUID) {
        currentPlayerScales.remove(playerUUID);
        targetPlayerScales.remove(playerUUID);
    }

    public static void clearAllPlayerScales() {
        currentPlayerScales.clear();
        targetPlayerScales.clear();
    }
}