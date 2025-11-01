package com.github.kd_gaming1.scaleme.client.util;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

/**
 * Utility class for applying scale transformations to matrix stacks.
 * Centralizes all transformation logic to ensure consistency.
 */
public final class ScaleTransformer {

    private ScaleTransformer() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Applies a uniform scale transformation if the scale is not default.
     * @param matrices The matrix stack to transform
     * @param scale The scale factor to apply
     */
    public static void applyScale(MatrixStack matrices, float scale) {
        if (matrices == null) {
            return;
        }

        if (!ScaleConstants.isDefaultScale(scale)) {
            float clampedScale = ScaleConstants.clampScale(scale);
            matrices.scale(clampedScale, clampedScale, clampedScale);
        }
    }

    /**
     * Applies rotation transformations in the correct order (pitch, yaw, roll).
     * @param matrices The matrix stack to transform
     * @param pitch Pitch rotation in degrees
     * @param yaw Yaw rotation in degrees
     * @param roll Roll rotation in degrees
     */
    public static void applyRotations(MatrixStack matrices, float pitch, float yaw, float roll) {
        if (matrices == null) {
            return;
        }

        if (pitch != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        }
        if (yaw != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        }
        if (roll != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
        }
    }

    /**
     * Applies position translation, accounting for scale.
     * Translation is divided by scale to maintain consistent positioning.
     * @param matrices The matrix stack to transform
     * @param x X position offset
     * @param y Y position offset
     * @param z Z position offset
     * @param scale The current scale factor (used to adjust translation)
     */
    public static void applyTranslation(MatrixStack matrices, float x, float y, float z, float scale) {
        if (matrices == null) {
            return;
        }

        if (x == 0.0f && y == 0.0f && z == 0.0f) {
            return;
        }

        float safeScale = Math.max(0.01f, scale); // Prevent division by zero
        matrices.translate(x / safeScale, y / safeScale, z / safeScale);
    }

    /**
     * Applies complete transformation for held items: rotation, scale, and position.
     * @param matrices The matrix stack to transform
     * @param config Item transformation configuration
     */
    public static void applyItemTransform(MatrixStack matrices, ItemTransformConfig config) {
        if (matrices == null || config == null) {
            return;
        }

        // Apply rotations first
        applyRotations(matrices, config.pitch, config.yaw, config.roll);

        // Apply scaling
        applyScale(matrices, config.scale);

        // Apply position adjustments
        applyTranslation(matrices, config.xPos, config.yPos, config.zPos, config.scale);
    }

    /**
     * Configuration class for item transformations.
     * Encapsulates all transformation parameters.
     */
    public static class ItemTransformConfig {
        public final float pitch;
        public final float yaw;
        public final float roll;
        public final float scale;
        public final float xPos;
        public final float yPos;
        public final float zPos;

        public ItemTransformConfig(float pitch, float yaw, float roll, float scale,
                                   float xPos, float yPos, float zPos) {
            this.pitch = pitch;
            this.yaw = yaw;
            this.roll = roll;
            this.scale = ScaleConstants.clampScale(scale);
            this.xPos = xPos;
            this.yPos = yPos;
            this.zPos = zPos;
        }

        /**
         * Creates a configuration from the mod's config values.
         */
        public static ItemTransformConfig fromConfig() {
            return new ItemTransformConfig(
                    ScaleMeConfig.heldItemPitchRotation,
                    ScaleMeConfig.heldItemYawRotation,
                    ScaleMeConfig.heldItemRollRotation,
                    ScaleMeConfig.itemScale,
                    ScaleMeConfig.heldItemXPosition,
                    ScaleMeConfig.heldItemYPosition,
                    ScaleMeConfig.heldItemZPosition
            );
        }

        /**
         * Checks if this configuration requires any transformation.
         */
        public boolean hasTransformations() {
            return pitch != 0.0f || yaw != 0.0f || roll != 0.0f ||
                    !ScaleConstants.isDefaultScale(scale) ||
                    xPos != 0.0f || yPos != 0.0f || zPos != 0.0f;
        }
    }
}