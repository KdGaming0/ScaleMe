package com.github.kd_gaming1.scaleme.client.data;

import com.github.kd_gaming1.scaleme.client.util.PlayerUUIDResolver;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

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


    public PlayerPreset(String identifier, String friendlyName, float scale) {
        this.identifier = identifier;
        this.friendlyName = friendlyName;
        this.scale = scale;
    }

    public PlayerPreset copy() {
        PlayerPreset copy = new PlayerPreset(this.identifier, this.friendlyName, this.scale);
        copy.enabled = this.enabled;
        return copy;
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

    /** Enhanced matching that works with both UUIDs and usernames. */
    public boolean matchesPlayer(UUID playerUUID, String playerName) {
        if (!enabled) return false;

        // Direct UUID match
        if (isUUID()) {
            try {
                return UUID.fromString(identifier).equals(playerUUID);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        // Username match (case insensitive)
        return playerName != null && identifier.equalsIgnoreCase(playerName);
    }

    /** Resolves this preset to a UUID if possible using the standard resolver. */
    public UUID resolveToUUID() {
        return PlayerUUIDResolver.resolvePlayerUUID(this.identifier);
    }
}