package com.github.kd_gaming1.scaleme.client.util;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;

import java.util.UUID;

/**
 * Simple facade for player scaling. All logic moved to PlayerPresetManager.
 * This class exists only for backwards compatibility and clean API.
 */
public class ScaleManager {

    /**
     * Initialize the scaling system.
     */
    public static void init() {
        PlayerPresetManager.init();
    }

    /**
     * Update all scales every tick.
     */
    public static void tick() {
        PlayerPresetManager.tick();
    }

    /**
     * Get current scale for a player.
     */
    public static float getCurrentScale(UUID playerUUID) {
        return PlayerPresetManager.getCurrentScale(playerUUID);
    }

    /** Returns NPC scale with safety check including dungeons detection. */
    public static float getNpcScale() {
        // Only apply scaling when it's safe (not in competitive modes or dungeons)
        if (HypixelDetector.isSafeGameMode() && HypixelDetector.isInDungeons()) return 1.0f;
        return ScaleMeConfig.npcPlayerScale;
    }

    /** Returns Villager NPC scale with safety check including dungeons detection. */
    public static float getVillagerNpcScale() {
        // Only apply scaling when it's safe (not in competitive modes or dungeons)
        if (HypixelDetector.isSafeGameMode() && HypixelDetector.isInDungeons()) return 1.0f;
        return ScaleMeConfig.villagerNpcScale;
    }
}