package com.github.kd_gaming1.scaleme.client.util;

/**
 * Centralized constants for the ScaleMe mod.
 * Contains all magic numbers, scale ranges, and default values.
 */
public final class ScaleConstants {

    // Prevent instantiation
    private ScaleConstants() {
        throw new AssertionError("Constants class should not be instantiated");
    }

    // ===== Scale Ranges =====
    public static final float MIN_SCALE = 0.1f;
    public static final float MAX_SCALE = 3.0f;
    public static final float DEFAULT_SCALE = 1.0f;

    // ===== Scale Thresholds =====
    public static final float SMALL_SCALE_THRESHOLD = 0.8f;
    public static final float LARGE_SCALE_THRESHOLD = 1.5f;

    // ===== Animation Constants =====
    public static final int DEFAULT_SWING_DURATION = 6;
    public static final int MIN_SWING_DURATION = 1;
    public static final int MAX_SWING_DURATION = 60;
    public static final float SWING_BOBBING_DISABLED_VALUE = 1.0f;

    // ===== UI Constants =====
    public static final int PLAYER_HEAD_SIZE = 24;
    public static final int PLAYER_HEAD_CONTAINER_PADDING = 4;
    public static final int SCALE_BAR_WIDTH = 40;
    public static final int SCALE_BAR_HEIGHT = 3;

    // ===== UUID Pattern =====
    public static final String UUID_REGEX = "^[0-9a-fA-F\\-]{36}$";
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,16}$";

    // ===== Validation Messages =====
    public static final String ERROR_INVALID_SCALE = "Scale must be between %.2f and %.2f";
    public static final String ERROR_NULL_IDENTIFIER = "Identifier cannot be null or empty";
    public static final String ERROR_INVALID_USERNAME = "Username must be 3-16 characters (alphanumeric and underscores only)";

    /**
     * Validates if a scale value is within acceptable range.
     * @param scale The scale value to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidScale(float scale) {
        return scale >= MIN_SCALE && scale <= MAX_SCALE && !Float.isNaN(scale) && !Float.isInfinite(scale);
    }

    /**
     * Clamps a scale value to the valid range.
     * @param scale The scale value to clamp
     * @return Clamped scale value
     */
    public static float clampScale(float scale) {
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            return DEFAULT_SCALE;
        }
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }

    /**
     * Checks if a scale value should be considered "tiny".
     * @param scale The scale value
     * @return true if scale is below small threshold
     */
    public static boolean isTinyScale(float scale) {
        return scale < SMALL_SCALE_THRESHOLD;
    }

    /**
     * Checks if a scale value should be considered "large".
     * @param scale The scale value
     * @return true if scale is above large threshold
     */
    public static boolean isLargeScale(float scale) {
        return scale > LARGE_SCALE_THRESHOLD;
    }

    /**
     * Checks if a scale value is effectively default (no scaling).
     * @param scale The scale value
     * @return true if scale is effectively 1.0
     */
    public static boolean isDefaultScale(float scale) {
        return Math.abs(scale - DEFAULT_SCALE) < 0.001f;
    }
}