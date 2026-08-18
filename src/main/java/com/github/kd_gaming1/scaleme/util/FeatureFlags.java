package com.github.kd_gaming1.scaleme.util;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;

/**
 * Centralised feature bitmask updated once per tick.
 * <p>
 * Hot render paths read a single {@code int} instead of multiple config booleans,
 * giving the JIT a trivial branch-predictor target and eliminating redundant
 * field accesses.
 * <p>
 * Some bits are <em>derived</em> rather than mapped 1:1 to a toggle — a scale slider
 * sitting at 1.0 is off, so the flag stays clear and the mixin early-outs without
 * ever comparing floats. Mixins should therefore test one bit, not a combination.
 */
public final class FeatureFlags {

    public static final int ARM_POSITION             = 1 << 0;
    public static final int ITEM_TRANSFORM           = 1 << 1;
    public static final int SEPARATE_OFFHAND         = 1 << 2;
    public static final int DISABLE_SWING_BOB        = 1 << 3;
    public static final int IGNORE_SWING_SPEED       = 1 << 4;
    public static final int SWING_DURATION           = 1 << 5;
    public static final int SWING_OVERRIDE           = 1 << 6;
    public static final int DISABLE_SWING_ANIM       = 1 << 7;
    public static final int SWORD_BLOCK              = 1 << 8;
    public static final int SCALE_NAME_TAGS          = 1 << 9;
    public static final int SCALE_ANY                = 1 << 10;
    public static final int CROSSHAIR_3RD            = 1 << 11;
    public static final int CROSSHAIR_3RD_FRONT      = 1 << 12;
    public static final int DISABLE_SELFIE           = 1 << 13;
    public static final int SHOW_OWN_NAMETAG         = 1 << 14;
    public static final int HIDE_PLAYERS             = 1 << 15;
    public static final int HIDE_PLAYERS_SB_ONLY     = 1 << 16;
    public static final int GROUND_ITEM_SCALE        = 1 << 17;
    public static final int SUPPRESS_REPEAT_SWING    = 1 << 18;
    public static final int HOLD_REPEAT_SWING_BOTTOM = 1 << 19;

    private static int flags = 0;

    private FeatureFlags() {}

    /** Recompute the bitmask from current config values. Call once per client tick. */
    public static void update() {
        int f = 0;

        if (ScaleMeConfig.enableArmPositionOverride)      f |= ARM_POSITION;
        if (ScaleMeConfig.enableItemTransformOverride)    f |= ITEM_TRANSFORM;
        if (ScaleMeConfig.enableSeparateHandTransforms)   f |= SEPARATE_OFFHAND;

        if (ScaleMeConfig.disableSwingBobbing)            f |= DISABLE_SWING_BOB;
        if (ScaleMeConfig.ignoreSwingSpeedEffects)        f |= IGNORE_SWING_SPEED;
        if (ScaleMeConfig.enableSwingOverride)            f |= SWING_OVERRIDE;
        if (ScaleMeConfig.disableSwingAnimation)          f |= DISABLE_SWING_ANIM;
        if (ScaleMeConfig.suppressRepeatedSwingAnimation) f |= SUPPRESS_REPEAT_SWING;
        if (ScaleMeConfig.holdRepeatedSwingsAtBottom)     f |= HOLD_REPEAT_SWING_BOTTOM;

        // Swing duration only needs touching when the base length or its modifiers change.
        if (ScaleMeConfig.ignoreSwingSpeedEffects
                || ScaleMeConfig.swingAnimationSpeed != 1f) f |= SWING_DURATION;

        if (ScaleMeConfig.enableSwordBlock)               f |= SWORD_BLOCK;

        if (ScaleMeConfig.scaleNameTags)                  f |= SCALE_NAME_TAGS;
        if (ScaleMeConfig.playerScale != 1f
                || ScaleMeConfig.otherPlayersScale != 1f
                || ScaleMeConfig.villagerNpcScale != 1f
                || ScaleMeConfig.hypixelNpcScale != 1f)   f |= SCALE_ANY;

        if (ScaleMeConfig.groundItemScale != 1f)          f |= GROUND_ITEM_SCALE;

        if (ScaleMeConfig.enableCrosshairInThirdPerson)   f |= CROSSHAIR_3RD;
        if (ScaleMeConfig.enableCrosshairInThirdPersonFront) f |= CROSSHAIR_3RD_FRONT;
        if (ScaleMeConfig.disableSelfieCam)               f |= DISABLE_SELFIE;
        if (ScaleMeConfig.showOwnNametagInThirdPerson)    f |= SHOW_OWN_NAMETAG;
        if (ScaleMeConfig.hidePlayers)                    f |= HIDE_PLAYERS;
        if (ScaleMeConfig.hidePlayersOnlyOnSkyblock)      f |= HIDE_PLAYERS_SB_ONLY;

        flags = f;
    }

    /** True when every bit in {@code mask} is set. */
    public static boolean isEnabled(int mask) {
        return (flags & mask) == mask;
    }

    /** True when at least one bit in {@code mask} is set. */
    public static boolean anyEnabled(int mask) {
        return (flags & mask) != 0;
    }
}
