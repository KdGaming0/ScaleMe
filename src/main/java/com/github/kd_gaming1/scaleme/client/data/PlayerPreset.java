package com.github.kd_gaming1.scaleme.client.data;

import com.github.kd_gaming1.scaleme.client.util.PlayerUUIDResolver;
import com.github.kd_gaming1.scaleme.client.util.ScaleConstants;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a scaling preset for a specific player.
 * <p>
 * Each preset contains:
 * <ul>
 *   <li>An identifier (UUID or username)</li>
 *   <li>An optional friendly display name</li>
 *   <li>A scale factor for the player</li>
 *   <li>An enabled/disabled state</li>
 * </ul>
 * <p>
 * Presets are immutable after creation, use {@link #copy()} to create modified versions.
 */
public class PlayerPreset {

    @SerializedName("identifier")
    public String identifier; // Package-private for manager access

    @SerializedName("friendlyName")
    public String friendlyName; // Package-private for manager access

    @SerializedName("scale")
    public float scale; // Package-private for manager access

    @SerializedName("enabled")
    public boolean enabled; // Package-private for manager access

    /**
     * Creates a new player preset with validation.
     * @param identifier The player identifier (UUID or username)
     * @param friendlyName Optional display name (can be null)
     * @param scale The scale factor
     * @throws IllegalArgumentException if identifier is invalid or scale is out of range
     */
    public PlayerPreset(String identifier, String friendlyName, float scale) {
        validateIdentifier(identifier);
        validateScale(scale);

        this.identifier = identifier.trim();
        this.friendlyName = (friendlyName != null && !friendlyName.trim().isEmpty())
                ? friendlyName.trim() : null;
        this.scale = ScaleConstants.clampScale(scale);
        this.enabled = true;
    }

    // ===== Validation Helper Methods =====

    /**
     * Validates and sets the scale value.
     * @param scale The new scale value
     * @throws IllegalArgumentException if scale is invalid
     */
    public void setScaleValidated(float scale) {
        validateScale(scale);
        this.scale = ScaleConstants.clampScale(scale);
    }

    /**
     * Validates and sets the identifier.
     * @param identifier The new identifier
     * @throws IllegalArgumentException if identifier is invalid
     */
    public void setIdentifierValidated(String identifier) {
        validateIdentifier(identifier);
        this.identifier = identifier.trim();
    }

    // ===== Core Methods =====

    /**
     * Creates a deep copy of this preset.
     * @return A new PlayerPreset with identical values
     */
    public PlayerPreset copy() {
        PlayerPreset copy = new PlayerPreset(this.identifier, this.friendlyName, this.scale);
        copy.enabled = this.enabled;
        return copy;
    }

    /**
     * Returns the display name for this preset.
     * Uses friendly name if available, otherwise falls back to identifier.
     * @return The effective display name
     */
    public String getEffectiveDisplayName() {
        return (friendlyName != null && !friendlyName.isEmpty())
                ? friendlyName
                : identifier;
    }

    /**
     * Checks if this preset matches a given identifier (case-insensitive).
     * Only returns true if the preset is enabled.
     * @param id The identifier to check
     * @return true if enabled and identifier matches
     */
    public boolean matches(String id) {
        return enabled && identifier.equalsIgnoreCase(id);
    }

    /**
     * Checks if this preset matches a player by UUID or username.
     * @param playerUUID The player's UUID
     * @param playerName The player's name (can be null)
     * @return true if enabled and matches either UUID or username
     */
    public boolean matchesPlayer(UUID playerUUID, String playerName) {
        if (!enabled || playerUUID == null) {
            return false;
        }

        // Check UUID match if identifier is a UUID
        if (isUUID()) {
            try {
                return UUID.fromString(identifier).equals(playerUUID);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        // Check username match (case insensitive)
        return playerName != null && identifier.equalsIgnoreCase(playerName);
    }

    /**
     * Attempts to resolve this preset's identifier to a UUID.
     * @return The resolved UUID, or null if resolution fails
     */
    public UUID resolveToUUID() {
        return PlayerUUIDResolver.resolvePlayerUUID(this.identifier);
    }

    // ===== Validation Methods =====

    /**
     * Checks if the identifier is in UUID format.
     * @return true if identifier matches UUID pattern
     */
    public boolean isUUID() {
        return identifier != null && identifier.matches(ScaleConstants.UUID_REGEX);
    }

    /**
     * Checks if the identifier is in username format.
     * @return true if identifier matches username pattern
     */
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

    /**
     * Validates that a scale value is within acceptable range.
     * @param scale The scale value to validate
     * @throws IllegalArgumentException if scale is invalid
     */
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