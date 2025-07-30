package com.github.kd_gaming1.scaleme.client.data;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a preset for scaling a player.
 */
public class PlayerPreset {
    @SerializedName("identifier")
    public String identifier; // UUID or username

    @SerializedName("friendlyName")
    public String friendlyName; // Optional friendly name

    @SerializedName("scale")
    public float scale;

    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("category")
    public String category;

    public PlayerPreset(String identifier, String friendlyName, float scale, String category) {
        this.identifier = identifier;
        this.friendlyName = friendlyName;
        this.scale = scale;
        this.category = category;
    }

    /** Checks if this preset matches the given identifier. */
    public boolean matches(String id) {
        return enabled && identifier.equalsIgnoreCase(id);
    }

    /** Returns the display name for this preset. */
    public String getEffectiveDisplayName() {
        return (friendlyName != null && !friendlyName.isEmpty()) ? friendlyName : identifier;
    }

    /** Returns true if the identifier is a UUID. */
    public boolean isUUID() {
        // Simple check for UUID format
        return identifier != null && identifier.matches("^[0-9a-fA-F\\-]{36}$");
    }
}