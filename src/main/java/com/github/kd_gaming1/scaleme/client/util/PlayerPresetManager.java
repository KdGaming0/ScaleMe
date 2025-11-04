package com.github.kd_gaming1.scaleme.client.util;

import com.github.kd_gaming1.scaleme.client.data.PlayerPreset;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Unified player scaling system with O(1) performance.
 * <p>
 * This manager handles:
 * <ul>
 *   <li>Player-specific presets (priority 1)</li>
 *   <li>Own player config scaling (priority 2)</li>
 *   <li>Global "other players" scaling (priority 3)</li>
 * </ul>
 * <p>
 * Provides smooth scale interpolation when enabled in config.
 * Thread-safe for concurrent access.
 */
public class PlayerPresetManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerPresetManager.class);
    private static final String PRESET_FILE_NAME = "scaleme_player_presets.json";

    // Smooth scaling configuration
    private static final float SMOOTH_INTERPOLATION_SPEED = 0.15f;
    private static final float SMOOTH_THRESHOLD = 0.001f;

    // Storage
    private static final List<PlayerPreset> presets = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<UUID, Float> currentScales = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Float> targetScales = new ConcurrentHashMap<>();

    // File I/O
    private static final File presetFile;
    private static final Gson gson;

    static {
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        presetFile = new File(configDir, PRESET_FILE_NAME);
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // ===== Initialization =====

    /**
     * Initializes the preset manager by loading presets from disk.
     * Should be called once during mod initialization.
     */
    public static void init() {
        loadPresets();
        refreshAllScales();
    }

    // ===== Scale Retrieval =====

    /**
     * Returns the current scale for a player with O(1) lookup.
     * <p>
     * Scale priority:
     * <ol>
     *   <li>Player-specific preset (if enabled)</li>
     *   <li>Own player config scale</li>
     *   <li>Global "other players" config scale</li>
     * </ol>
     * <p>
     * Returns 1.0 if:
     * <ul>
     *   <li>playerUUID is null</li>
     *   <li>In a safe game mode (minigame) - scaling disabled for competitive gameplay</li>
     * </ul>
     *
     * @param playerUUID The player's UUID
     * @return The current scale factor (1.0 = normal size)
     */
    public static float getCurrentScale(UUID playerUUID) {
        // Safety checks - disable scaling in minigames or if UUID is null
        // isSafeGameMode() returns true when in a minigame (not lobby/housing/etc)
        if (HypixelDetector.isSafeGameMode() || playerUUID == null) {
            return ScaleConstants.DEFAULT_SCALE;
        }

        // Check cache first
        Float cachedScale = currentScales.get(playerUUID);
        if (cachedScale != null) {
            return cachedScale;
        }

        // Determine and cache the scale
        float scale = determineScale(playerUUID);
        currentScales.put(playerUUID, scale);
        targetScales.put(playerUUID, scale);

        return scale;
    }

    /**
     * Updates all scales every tick with optional smooth interpolation.
     * Should be called every client tick.
     */
    public static void tick() {
        refreshConfigBasedScales();

        if (ScaleMeConfig.smoothScaling) {
            applySmoothScaling();
        } else {
            applyInstantScaling();
        }
    }

    // ===== Preset Management =====

    /**
     * Adds or updates a player preset.
     * Automatically saves to disk and refreshes all scales.
     *
     * @param preset The preset to add or update
     */
    public static void addOrUpdatePreset(PlayerPreset preset) {
        if (!isValidPreset(preset)) {
            LOGGER.warn("Attempted to add invalid preset: {}", preset);
            return;
        }

        // Remove existing preset with same identifier
        removePresetQuiet(preset.identifier);

        presets.add(preset);
        savePresets();
        refreshAllScales();
    }

    /**
     * Removes a preset by identifier.
     *
     * @param identifier The preset identifier (UUID or username)
     * @return true if a preset was removed
     */
    public static boolean removePreset(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        boolean removed = presets.removeIf(p ->
                p.identifier.equalsIgnoreCase(identifier)
        );

        if (removed) {
            savePresets();
            refreshAllScales();
        }

        return removed;
    }

    /**
     * Gets a preset by identifier.
     *
     * @param identifier The preset identifier
     * @return The preset, or null if not found
     */
    public static PlayerPreset getPreset(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return null;
        }

        return presets.stream()
                .filter(p -> p.identifier.equalsIgnoreCase(identifier))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a defensive copy of all presets.
     *
     * @return List of all presets
     */
    public static List<PlayerPreset> getAllPresets() {
        return new ArrayList<>(presets);
    }

    /**
     * Enables or disables a preset.
     *
     * @param identifier The preset identifier
     * @param enabled Whether the preset should be enabled
     * @return true if the preset was found and updated
     */
    public static boolean setPresetEnabled(String identifier, boolean enabled) {
        PlayerPreset preset = getPreset(identifier);
        if (preset == null) {
            return false;
        }

        preset.enabled = enabled;
        savePresets();
        refreshAllScales();
        return true;
    }

    // ===== File I/O =====

    /**
     * Loads presets from disk.
     * Creates an empty preset file if none exists.
     */
    public static void loadPresets() {
        presets.clear();

        if (!presetFile.exists()) {
            LOGGER.info("No preset file found, creating empty file");
            savePresets();
            return;
        }

        try (FileReader reader = new FileReader(presetFile)) {
            Type listType = new TypeToken<List<PlayerPreset>>(){}.getType();
            List<PlayerPreset> loaded = gson.fromJson(reader, listType);

            if (loaded != null) {
                presets.addAll(loaded);
                LOGGER.info("Loaded {} preset(s) from disk", presets.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load presets: {}", e.getMessage(), e);
        }
    }

    /**
     * Saves all presets to disk.
     */
    public static void savePresets() {
        try {
            // Ensure directory exists
            File parentDir = presetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileWriter writer = new FileWriter(presetFile)) {
                gson.toJson(presets, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save presets: {}", e.getMessage(), e);
        }
    }

    // ===== Private Helper Methods =====

    /**
     * Determines the appropriate scale for a player based on priority.
     */
    private static float determineScale(UUID playerUUID) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Get player name for username-based preset matching
        String playerName = getPlayerName(playerUUID, client);

        // Priority 1: Check enabled presets
        for (PlayerPreset preset : presets) {
            if (preset.matchesPlayer(playerUUID, playerName)) {
                return ScaleConstants.clampScale(preset.scale);
            }
        }

        // Priority 2: Own player scale from config
        if (isOwnPlayer(playerUUID, client)) {
            return ScaleConstants.clampScale(ScaleMeConfig.ownPlayerScale);
        }

        // Priority 3: Default "other players" scale from config (if enabled)
        if (ScaleMeConfig.enableOtherPlayersScaling) {
            return ScaleConstants.clampScale(ScaleMeConfig.otherPlayersScale);
        }

        // If other players scaling is disabled, return default (no scaling)
        return ScaleConstants.DEFAULT_SCALE;
    }

    /**
     * Applies smooth scale interpolation.
     */
    private static void applySmoothScaling() {
        for (var entry : targetScales.entrySet()) {
            UUID uuid = entry.getKey();
            float target = entry.getValue();
            float current = currentScales.getOrDefault(uuid, target);

            float smoothed = interpolateScale(current, target);
            currentScales.put(uuid, smoothed);
        }
    }

    /**
     * Applies instant scale changes (no interpolation).
     */
    private static void applyInstantScaling() {
        currentScales.putAll(targetScales);
    }

    /**
     * Smoothly interpolates between current and target scale.
     */
    private static float interpolateScale(float current, float target) {
        float difference = target - current;

        if (Math.abs(difference) <= SMOOTH_THRESHOLD) {
            return target; // Close enough, snap to target
        }

        return current + (difference * SMOOTH_INTERPOLATION_SPEED);
    }

    /**
     * Refreshes all target scales from config and presets.
     */
    private static void refreshAllScales() {
        refreshConfigBasedScales();
        refreshPresetBasedScales();
        cleanupStaleScales();
    }

    /**
     * Updates scales for own player and global "other players" default.
     */
    private static void refreshConfigBasedScales() {
        MinecraftClient client = MinecraftClient.getInstance();
        UUID ownUUID = client.player != null ? client.player.getUuid() : null;

        // Update own player scale
        if (ownUUID != null) {
            targetScales.put(ownUUID, ScaleMeConfig.ownPlayerScale);
        }

        // Update other players without presets (only if enabled)
        if (ScaleMeConfig.enableOtherPlayersScaling) {
            updateOtherPlayersScale(ownUUID);
        } else {
            // If disabled, remove scales for players without presets
            removeNonPresetPlayerScales(ownUUID);
        }
    }

    /**
     * Updates target scales for all enabled presets.
     */
    private static void refreshPresetBasedScales() {
        for (PlayerPreset preset : presets) {
            if (!preset.enabled) {
                continue;
            }

            UUID resolvedUUID = preset.resolveToUUID();
            if (resolvedUUID != null) {
                targetScales.put(resolvedUUID, preset.scale);
            }
        }
    }

    /**
     * Updates scales for players without presets to use the global default.
     */
    private static void updateOtherPlayersScale(UUID ownUUID) {
        float defaultScale = ScaleMeConfig.otherPlayersScale;

        for (UUID uuid : targetScales.keySet()) {
            // Skip own player
            if (uuid.equals(ownUUID)) {
                continue;
            }

            // Skip if player has an enabled preset
            if (hasEnabledPreset(uuid)) {
                continue;
            }

            // Apply global default
            targetScales.put(uuid, defaultScale);
        }
    }

    /**
     * Removes scales for players without presets (when other players scaling is disabled).
     */
    private static void removeNonPresetPlayerScales(UUID ownUUID) {
        // Collect UUIDs to remove
        List<UUID> toRemove = new ArrayList<>();

        for (UUID uuid : targetScales.keySet()) {
            // Keep own player
            if (uuid.equals(ownUUID)) {
                continue;
            }

            // Keep if player has an enabled preset
            if (hasEnabledPreset(uuid)) {
                continue;
            }

            // Remove this player's scale
            toRemove.add(uuid);
        }

        // Remove collected UUIDs
        for (UUID uuid : toRemove) {
            targetScales.remove(uuid);
            currentScales.remove(uuid);
        }
    }

    /**
     * Removes scales for players no longer in the target map.
     */
    private static void cleanupStaleScales() {
        currentScales.keySet().retainAll(targetScales.keySet());
    }

    /**
     * Checks if a player has an enabled preset.
     */
    private static boolean hasEnabledPreset(UUID uuid) {
        String uuidString = uuid.toString();
        return presets.stream()
                .anyMatch(p -> p.enabled &&
                        p.isUUID() &&
                        p.identifier.equalsIgnoreCase(uuidString));
    }

    /**
     * Gets the player name from the player list or current player.
     */
    private static String getPlayerName(UUID playerUUID, MinecraftClient client) {
        if (client.getNetworkHandler() != null) {
            var entry = client.getNetworkHandler().getPlayerListEntry(playerUUID);
            if (entry != null) {
                //? if >=1.21.9 {
                /*return entry.getProfile().name();
                *///?} else {
                return entry.getProfile().getName();
                 //?}
            }
        }
        return null;
    }

    /**
     * Checks if a UUID belongs to the current player.
     */
    private static boolean isOwnPlayer(UUID playerUUID, MinecraftClient client) {
        return client.player != null && playerUUID.equals(client.player.getUuid());
    }

    /**
     * Validates a preset before adding it.
     */
    private static boolean isValidPreset(PlayerPreset preset) {
        return preset != null &&
                preset.identifier != null &&
                !preset.identifier.isEmpty() &&
                ScaleConstants.isValidScale(preset.scale);
    }

    /**
     * Removes a preset without triggering save/refresh (internal use).
     */
    private static void removePresetQuiet(String identifier) {
        presets.removeIf(p -> p.identifier.equalsIgnoreCase(identifier));
    }
}