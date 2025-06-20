package com.github.scaleme.client.util;

import com.github.scaleme.config.ScaleMeConfig;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class ScaleManager {
    private static float currentOwnScale = 1.0f;
    private static float targetOwnScale = 1.0f;
    private static float currentOtherScale = 1.0f;
    private static float targetOtherScale = 1.0f;
    private static long lastUpdateTime = 0;

    public static void init() {
        currentOwnScale = ScaleMeConfig.ownPlayerScale;
        targetOwnScale = ScaleMeConfig.ownPlayerScale;
        currentOtherScale = ScaleMeConfig.otherPlayersScale;
        targetOtherScale = ScaleMeConfig.otherPlayersScale;
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
    }

    public static float getCurrentScale(UUID playerUUID) {
        tick();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || playerUUID == null) {
            return 1.0f;
        }

        boolean isOwnPlayer = playerUUID.equals(client.player.getUuid());

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
}