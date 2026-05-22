package com.github.kd_gaming1.scaleme.util;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;

/**
 * Centralised feature bitmask updated once per tick.
 * <p>
 * Hot render paths read a single {@code int} instead of multiple config booleans,
 * giving the JIT a trivial branch-predictor target and eliminating redundant
 * field accesses.
 */
public final class FeatureFlags {

    public static final int HAND_TRANSFORM           = 1 << 0;
    public static final int ARM_POSITION             = 1 << 1;
    public static final int ITEM_TRANSFORM           = 1 << 2;
    public static final int SEPARATE_OFFHAND         = 1 << 3;
    public static final int ANIM_OVERRIDES           = 1 << 4;
    public static final int DISABLE_SWING_BOB        = 1 << 5;
    public static final int IGNORE_SWING_SPEED       = 1 << 6;
    public static final int SWING_OVERRIDE           = 1 << 7;
    public static final int DISABLE_SWING_ANIM       = 1 << 8;
    public static final int SWORD_BLOCK              = 1 << 9;
    public static final int SCALE_NAME_TAGS          = 1 << 10;
    public static final int SCALE_ANY                = 1 << 11;
    public static final int CROSSHAIR_3RD            = 1 << 12;
    public static final int CROSSHAIR_3RD_FRONT      = 1 << 13;
    public static final int DISABLE_SELFIE           = 1 << 14;
    public static final int SHOW_OWN_NAMETAG         = 1 << 15;
    public static final int HIDE_PLAYERS             = 1 << 16;
    public static final int HIDE_PLAYERS_SB_ONLY     = 1 << 17;
    public static final int GROUND_ITEM_SCALE        = 1 << 18;

    private static int flags = 0;

    private FeatureFlags() {}

    /** Recompute the bitmask from current config values. Call once per client tick. */
    public static void update() {
        int f = 0;

        if (ScaleMeConfig.enableHandItemTransform)        f |= HAND_TRANSFORM;
        if (ScaleMeConfig.enableArmPositionOverride)      f |= ARM_POSITION;
        if (ScaleMeConfig.enableItemTransformOverride)    f |= ITEM_TRANSFORM;
        if (ScaleMeConfig.enableSeparateHandTransforms)   f |= SEPARATE_OFFHAND;

        if (ScaleMeConfig.enableAnimOverrides)            f |= ANIM_OVERRIDES;
        if (ScaleMeConfig.disableSwingBobbing)            f |= DISABLE_SWING_BOB;
        if (ScaleMeConfig.ignoreSwingSpeedEffects)        f |= IGNORE_SWING_SPEED;
        if (ScaleMeConfig.enableSwingOverride)            f |= SWING_OVERRIDE;
        if (ScaleMeConfig.disableSwingAnimation)          f |= DISABLE_SWING_ANIM;

        if (ScaleMeConfig.enableSwordBlock)               f |= SWORD_BLOCK;

        if (ScaleMeConfig.scaleNameTags)                  f |= SCALE_NAME_TAGS;
        if (ScaleMeConfig.playerScale != 1f
                || ScaleMeConfig.otherPlayersScale != 1f
                || ScaleMeConfig.villagerNpcScale != 1f
                || ScaleMeConfig.hypixelNpcScale != 1f)   f |= SCALE_ANY;

        if (ScaleMeConfig.enableCrosshairInThirdPerson)   f |= CROSSHAIR_3RD;
        if (ScaleMeConfig.enableCrosshairInThirdPersonFront) f |= CROSSHAIR_3RD_FRONT;
        if (ScaleMeConfig.disableSelfieCam)               f |= DISABLE_SELFIE;
        if (ScaleMeConfig.showOwnNametagInThirdPerson)    f |= SHOW_OWN_NAMETAG;
        if (ScaleMeConfig.hidePlayers)                    f |= HIDE_PLAYERS;
        if (ScaleMeConfig.hidePlayersOnlyOnSkyblock)      f |= HIDE_PLAYERS_SB_ONLY;

        if (ScaleMeConfig.enableGroundItemScale)          f |= GROUND_ITEM_SCALE;

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
