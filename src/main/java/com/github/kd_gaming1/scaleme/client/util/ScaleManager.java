package com.github.kd_gaming1.scaleme.client.util;

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
}