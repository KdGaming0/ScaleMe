package com.github.scaleme.client.util;

import com.github.scaleme.Scaleme;
import com.github.scaleme.client.data.PlayerPreset;
import com.github.scaleme.config.ScaleMeConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerPresetManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, PlayerPreset> presetsByUUID = new ConcurrentHashMap<>();
    private static final Map<String, PlayerPreset> presetsByUsername = new ConcurrentHashMap<>();
    private static final Map<String, String> uuidToUsernameCache = new ConcurrentHashMap<>();
    private static final Set<String> availableCategories = new LinkedHashSet<>();
    private static File configFile;

    // Default categories
    public static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Default", "Friends", "Guild", "Content Creators"
    );

    public static void init() {
        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "scaleme_presets.json");
        loadPresets();

        // Initialize default categories
        availableCategories.addAll(DEFAULT_CATEGORIES);

        // Create default examples if file doesn't exist
        if (!configFile.exists()) {
            createDefaultPresets();
        }
    }

    private static void createDefaultPresets() {
        List<PlayerPreset> defaultPresets = Arrays.asList(
                new PlayerPreset("example_uuid_here", "Guild Leader", 1.5f, "guild"),
                new PlayerPreset("another_example_uuid", "Best Friend", 2.0f, "friends"),
                new PlayerPreset("Notch", "", 1.2f, "special")
        );

        savePresets(defaultPresets);
        Scaleme.LOGGER.info("Created default player presets file with examples");
    }

    public static void loadPresets() {
        if (!configFile.exists()) return;

        try (FileReader reader = new FileReader(configFile)) {
            Type listType = new TypeToken<List<PlayerPreset>>(){}.getType();
            List<PlayerPreset> presets = GSON.fromJson(reader, listType);

            if (presets != null) {
                presetsByUUID.clear();
                presetsByUsername.clear();
                availableCategories.clear();
                availableCategories.addAll(DEFAULT_CATEGORIES);

                for (PlayerPreset preset : presets) {
                    if (preset.isUUID()) {
                        presetsByUUID.put(preset.identifier.toLowerCase(), preset);
                    } else {
                        presetsByUsername.put(preset.identifier.toLowerCase(), preset);
                    }

                    // Collect categories
                    if (preset.category != null && !preset.category.trim().isEmpty()) {
                        availableCategories.add(preset.category.trim());
                    }
                }

                Scaleme.LOGGER.info("Loaded {} player presets", presets.size());
            }
        } catch (IOException e) {
            Scaleme.LOGGER.error("Failed to load player presets", e);
        }
    }

    public static void savePresets(List<PlayerPreset> presets) {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(presets, writer);
            Scaleme.LOGGER.info("Saved {} player presets", presets.size());
        } catch (IOException e) {
            Scaleme.LOGGER.error("Failed to save player presets", e);
        }
    }

    public static PlayerPreset getPresetForPlayer(UUID playerUUID) {
        if (!ScaleMeConfig.enablePlayerPresets) return null;

        String uuidString = playerUUID.toString();

        // Check UUID-based presets first
        PlayerPreset preset = presetsByUUID.get(uuidString.toLowerCase());
        if (preset != null && preset.enabled) {
            return preset;
        }

        // Check username-based presets
        String username = getPlayerName(playerUUID);
        if (username != null) {
            preset = presetsByUsername.get(username.toLowerCase());
            if (preset != null && preset.enabled) {
                return preset;
            }
        }

        return null;
    }

    private static String getPlayerName(UUID playerUUID) {
        // Check cache first
        String cached = uuidToUsernameCache.get(playerUUID.toString());
        if (cached != null) return cached;

        // Get from player list
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(playerUUID);
            if (entry != null && entry.getProfile().getName() != null) {
                String username = entry.getProfile().getName();
                uuidToUsernameCache.put(playerUUID.toString(), username);
                return username;
            }
        }

        return null;
    }

    public static void addPreset(PlayerPreset preset) {
        if (preset.isUUID()) {
            presetsByUUID.put(preset.identifier.toLowerCase(), preset);
        } else {
            presetsByUsername.put(preset.identifier.toLowerCase(), preset);
        }

        // Add category to available categories
        if (preset.category != null && !preset.category.trim().isEmpty()) {
            availableCategories.add(preset.category.trim());
        }

        // Save to file
        List<PlayerPreset> allPresets = new ArrayList<>();
        allPresets.addAll(presetsByUUID.values());
        allPresets.addAll(presetsByUsername.values());
        savePresets(allPresets);
    }

    public static void removePreset(String identifier, boolean isUUID) {
        if (isUUID) {
            presetsByUUID.remove(identifier.toLowerCase());
        } else {
            presetsByUsername.remove(identifier.toLowerCase());
        }

        // Save to file
        List<PlayerPreset> allPresets = new ArrayList<>();
        allPresets.addAll(presetsByUUID.values());
        allPresets.addAll(presetsByUsername.values());
        savePresets(allPresets);
    }

    public static List<PlayerPreset> getAllPresets() {
        List<PlayerPreset> allPresets = new ArrayList<>();
        allPresets.addAll(presetsByUUID.values());
        allPresets.addAll(presetsByUsername.values());
        return allPresets;
    }

    public static List<PlayerPreset> getFilteredPresets(String searchTerm, String categoryFilter, SortType sortType) {
        List<PlayerPreset> presets = getAllPresets();

        // Apply search filter
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String search = searchTerm.toLowerCase().trim();
            presets = presets.stream()
                    .filter(preset ->
                            preset.getEffectiveDisplayName().toLowerCase().contains(search) ||
                                    preset.identifier.toLowerCase().contains(search) ||
                                    preset.category.toLowerCase().contains(search)
                    )
                    .collect(Collectors.toList());
        }

        // Apply category filter
        if (categoryFilter != null && !categoryFilter.equals("all")) {
            presets = presets.stream()
                    .filter(preset -> preset.category.equals(categoryFilter))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        switch (sortType) {
            case NAME:
                presets.sort(Comparator.comparing(PlayerPreset::getEffectiveDisplayName, String.CASE_INSENSITIVE_ORDER));
                break;
            case CATEGORY:
                presets.sort(Comparator.comparing((PlayerPreset p) -> p.category, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PlayerPreset::getEffectiveDisplayName, String.CASE_INSENSITIVE_ORDER));
                break;
            case SCALE:
                presets.sort(Comparator.comparing((PlayerPreset p) -> p.scale).reversed());
                break;
            case ENABLED:
                presets.sort(Comparator.comparing((PlayerPreset p) -> p.enabled).reversed()
                        .thenComparing(PlayerPreset::getEffectiveDisplayName, String.CASE_INSENSITIVE_ORDER));
                break;
        }

        return presets;
    }

    public static Set<String> getAvailableCategories() {
        return new LinkedHashSet<>(availableCategories);
    }

    public static void addCategory(String category) {
        if (category != null && !category.trim().isEmpty()) {
            availableCategories.add(category.trim());
        }
    }

    public static void clearCache() {
        uuidToUsernameCache.clear();
    }

    public enum SortType {
        NAME, CATEGORY, SCALE, ENABLED
    }
}