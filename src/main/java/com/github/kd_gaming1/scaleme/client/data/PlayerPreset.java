package com.github.kd_gaming1.scaleme.client.data;

import com.github.kd_gaming1.scaleme.client.util.PlayerUUIDResolver;
import com.github.kd_gaming1.scaleme.client.util.ScaleConstants;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;
import java.util.UUID;

public class PlayerPreset {

    @SerializedName("identifier")
    public String identifier;

    @SerializedName("friendlyName")
    public String friendlyName;

    @SerializedName("scale")
    public float scale;

    @SerializedName("enabled")
    public boolean enabled;

    /**
     * Creates a new player preset with validation.
     * @param identifier The player identifier (UUID or username)
     * @param friendlyName Optional display name (can be null)
     * @param scale The scale factor
     * @throws IllegalArgumentException if scale is out of range
     */
    public PlayerPreset(String identifier, String friendlyName, float scale) {
        // Allow empty identifier for draft presets
        // Validation will occur during save operation
        this.identifier = identifier != null ? identifier.trim() : "";
        this.friendlyName = (friendlyName != null && !friendlyName.trim().isEmpty())
                ? friendlyName.trim() : null;

        validateScale(scale);
        this.scale = ScaleConstants.clampScale(scale);
        this.enabled = true;
    }

    /**
     * Validates that this preset is ready to be saved.
     * @throws IllegalArgumentException if preset is invalid
     */
    public void validateForSave() {
        validateIdentifier(this.identifier);
        validateScale(this.scale);
    }

    /**
     * Checks if this preset is valid for saving.
     * @return true if valid, false otherwise
     */
    public boolean isValidForSave() {
        try {
            validateForSave();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ===== Validation Helper Methods =====

    public void setScaleValidated(float scale) {
        validateScale(scale);
        this.scale = ScaleConstants.clampScale(scale);
    }

    public void setIdentifierValidated(String identifier) {
        validateIdentifier(identifier);
        this.identifier = identifier.trim();
    }

    // ===== Core Methods =====

    public PlayerPreset copy() {
        PlayerPreset copy = new PlayerPreset(this.identifier, this.friendlyName, this.scale);
        copy.enabled = this.enabled;
        return copy;
    }

    public String getEffectiveDisplayName() {
        return (friendlyName != null && !friendlyName.isEmpty())
                ? friendlyName
                : identifier;
    }

    public boolean matches(String id) {
        return enabled && identifier.equalsIgnoreCase(id);
    }

    public boolean matchesPlayer(UUID playerUUID, String playerName) {
        if (!enabled || playerUUID == null) {
            return false;
        }

        if (isUUID()) {
            try {
                return UUID.fromString(identifier).equals(playerUUID);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        return playerName != null && identifier.equalsIgnoreCase(playerName);
    }

    public UUID resolveToUUID() {
        return PlayerUUIDResolver.resolvePlayerUUID(this.identifier);
    }

    // ===== Validation Methods =====

    public boolean isUUID() {
        return identifier != null && identifier.matches(ScaleConstants.UUID_REGEX);
    }

    public boolean isUsername() {
        return identifier != null && identifier.matches(ScaleConstants.USERNAME_REGEX);
    }

    /**
     * Validates that an identifier is not null and not empty.
     * @param identifier The identifier to validate
     * @throws IllegalArgumentException if identifier is invalid
     */
    private void validateIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException(ScaleConstants.ERROR_NULL_IDENTIFIER);
        }
    }

    private void validateScale(float scale) {
        if (!ScaleConstants.isValidScale(scale)) {
            throw new IllegalArgumentException(
                    String.format(ScaleConstants.ERROR_INVALID_SCALE,
                            ScaleConstants.MIN_SCALE,
                            ScaleConstants.MAX_SCALE)
            );
        }
    }

    // ===== Object Methods =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerPreset that = (PlayerPreset) o;
        return Float.compare(that.scale, scale) == 0 &&
                enabled == that.enabled &&
                Objects.equals(identifier, that.identifier) &&
                Objects.equals(friendlyName, that.friendlyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, friendlyName, scale, enabled);
    }

    @Override
    public String toString() {
        return String.format("PlayerPreset{identifier='%s', friendlyName='%s', scale=%.2f, enabled=%s}",
                identifier, friendlyName, scale, enabled);
    }
}