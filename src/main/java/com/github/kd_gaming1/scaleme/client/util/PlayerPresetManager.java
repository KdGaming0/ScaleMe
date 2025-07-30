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
 * Unified player scaling system. Handles both presets and global config scaling.
 * Single source of truth for all player scales with O(1) performance.
 */
public class PlayerPresetManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerPresetManager.class);
    private static final String PRESET_FILE_NAME = "scaleme_player_presets.json";

    // Preset storage
    private static final List<PlayerPreset> presets = new CopyOnWriteArrayList<>();
    private static final File presetFile;
    private static final Gson gson;

    // Unified scale storage
    private static final ConcurrentHashMap<UUID, Float> currentScales = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Float> targetScales = new ConcurrentHashMap<>();

    // Default scales for fallback
    private static float defaultOtherScale = 1.0f;

    static {
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        presetFile = new File(configDir, PRESET_FILE_NAME);
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // === Initialization ===

    /** Loads presets and sets up scales. */
    public static void init() {
        loadPresets();
        refreshAllScales();
    }

    // === Scale Management ===

    /** Updates all scales every tick with smooth scaling if enabled. */
    public static void tick() {
        refreshTargetsFromConfig();

        if (ScaleMeConfig.smoothScaling) {
            for (var entry : targetScales.entrySet()) {
                UUID uuid = entry.getKey();
                float target = entry.getValue();
                float current = currentScales.getOrDefault(uuid, target);
                currentScales.put(uuid, smoothScale(current, target));
            }
        } else {
            currentScales.putAll(targetScales);
        }
    }

    /** Returns the current scale for a player. O(1) lookup. */
    public static float getCurrentScale(UUID playerUUID) {
        if (!HypixelDetector.isSafeGameMode()) return 1.0f; // Disable scaling if not safe game mode
        if (playerUUID == null) return 1.0f;

        Float cached = currentScales.get(playerUUID);
        if (cached != null) return cached;

        float scale = determineScale(playerUUID);
        currentScales.put(playerUUID, scale);
        targetScales.put(playerUUID, scale);

        return scale;
    }

    /** Determines the scale for a player (preset > own player config > other player config). */
    private static float determineScale(UUID playerUUID) {
        for (PlayerPreset preset : presets) {
            if (preset.enabled && preset.isUUID()) {
                try {
                    if (UUID.fromString(preset.identifier).equals(playerUUID)) {
                        return preset.scale;
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && playerUUID.equals(client.player.getUuid())) {
            return ScaleMeConfig.ownPlayerScale;
        }

        return defaultOtherScale;
    }

    /** Smoothly interpolates between current and target scale. */
    private static float smoothScale(float current, float target) {
        float diff = target - current;
        return Math.abs(diff) > 0.001f ? current + diff * 0.15f : target;
    }

    /** Refreshes all target scales from config and presets. */
    private static void refreshAllScales() {
        refreshTargetsFromConfig();

        for (PlayerPreset preset : presets) {
            if (preset.enabled && preset.isUUID()) {
                try {
                    UUID uuid = UUID.fromString(preset.identifier);
                    targetScales.put(uuid, preset.scale);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        currentScales.keySet().retainAll(targetScales.keySet());
    }

    /** Refreshes config-based scales (own player + update default). */
    private static void refreshTargetsFromConfig() {
        defaultOtherScale = ScaleMeConfig.otherPlayersScale;

        MinecraftClient client = MinecraftClient.getInstance();
        UUID ownUUID = client.player != null ? client.player.getUuid() : null;

        if (ownUUID != null) {
            targetScales.put(ownUUID, ScaleMeConfig.ownPlayerScale);
        }

        for (UUID uuid : targetScales.keySet()) {
            if (uuid.equals(ownUUID)) continue;
            boolean hasPreset = presets.stream().anyMatch(p -> p.enabled && p.isUUID() && uuid.toString().equalsIgnoreCase(p.identifier));
            if (!hasPreset) {
                targetScales.put(uuid, defaultOtherScale);
            }
        }
    }

    // === Preset Management ===

    /** Loads presets from disk. */
    public static void loadPresets() {
        presets.clear();
        if (!presetFile.exists()) {
            savePresets();
            return;
        }

        try (FileReader reader = new FileReader(presetFile)) {
            Type listType = new TypeToken<List<PlayerPreset>>(){}.getType();
            List<PlayerPreset> loaded = gson.fromJson(reader, listType);
            if (loaded != null) {
                presets.addAll(loaded);
                LOGGER.info("Loaded {} presets", presets.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load presets: {}", e.getMessage());
        }
    }

    /** Saves presets to disk. */
    public static void savePresets() {
        try {
            presetFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(presetFile)) {
                gson.toJson(presets, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save presets: {}", e.getMessage());
        }
    }

    /** Adds or updates a preset, then saves and refreshes scales. */
    public static void addOrUpdatePreset(PlayerPreset preset) {
        if (preset == null || preset.identifier == null) return;

        presets.removeIf(p -> p.identifier.equalsIgnoreCase(preset.identifier));
        presets.add(preset);
        savePresets();
        refreshAllScales();
    }

    /** Removes a preset by identifier. */
    public static boolean removePreset(String identifier) {
        if (identifier == null) return false;

        boolean removed = presets.removeIf(p -> p.identifier.equalsIgnoreCase(identifier));
        if (removed) {
            savePresets();
            refreshAllScales();
        }
        return removed;
    }

    /** Gets a preset by identifier. */
    public static PlayerPreset getPreset(String identifier) {
        return identifier == null ? null :
                presets.stream()
                        .filter(p -> p.identifier.equalsIgnoreCase(identifier))
                        .findFirst()
                        .orElse(null);
    }

    /** Returns a copy of all presets. */
    public static List<PlayerPreset> getAllPresets() {
        return new ArrayList<>(presets);
    }

    /** Enables or disables a preset by identifier. */
    public static boolean setPresetEnabled(String identifier, boolean enabled) {
        PlayerPreset preset = getPreset(identifier);
        if (preset != null) {
            preset.enabled = enabled;
            savePresets();
            refreshAllScales();
            return true;
        }
        return false;
    }
}