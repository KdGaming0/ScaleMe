package com.github.scaleme.client.data;

import com.google.gson.annotations.SerializedName;

public class PlayerPreset {
    @SerializedName("identifier")
    public String identifier; // UUID or username

    @SerializedName("displayName")
    public String displayName; // Optional friendly name for the preset

    @SerializedName("scale")
    public float scale;

    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("category")
    public String category = "default";

    public PlayerPreset() {}

    public PlayerPreset(String identifier, String displayName, float scale, String category) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.scale = scale;
        this.category = category;
    }

    // Auto-detect if identifier is UUID
    public boolean isUUID() {
        if (identifier == null) return false;
        // UUID format: 8-4-4-4-12 characters with hyphens
        return identifier.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    // Get effective display name (falls back to identifier if not set)
    public String getEffectiveDisplayName() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim();
        }
        // For UUIDs, show a shortened version as fallback
        if (isUUID()) {
            return identifier.substring(0, 8) + "...";
        }
        return identifier;
    }

    public boolean matches(String uuid, String username) {
        if (!enabled) return false;

        if (isUUID()) {
            return identifier.equalsIgnoreCase(uuid);
        } else {
            return identifier.equalsIgnoreCase(username);
        }
    }
}