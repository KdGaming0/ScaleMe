package com.github.kd_gaming1.scaleme.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ScaleMeConfig extends MidnightConfig {
    public static final String SCALING = "scaling";
    public static final String HAND = "hand_item";
    public static final String ANIM = "animation";
    public static final String VIEW = "view";

    // ── Hand Item Transform ─────────────────────────────────────────────────

    @Comment(category = HAND)
    public static Comment handDesc;

    @Entry(category = HAND)
    public static boolean enableHandItemTransform = false;

    @Comment(category = HAND, centered = true)
    public static Comment spacer1;

    @Entry(category = HAND, name = "Scale", isSlider = true, min = 0.1f, max = 4f, precision = 1000)
    public static float itemScale = 1f;

    @Comment(category = HAND, centered = true)
    public static Comment spacer2;

    @Entry(category = HAND, isSlider = true, min = -1.5f, max = 1.5f, precision = 1000)
    public static float itemTranslationX = 0f;

    @Entry(category = HAND, isSlider = true, min = -1.5f, max = 1.5f, precision = 1000)
    public static float itemTranslationY = 0f;

    @Entry(category = HAND, isSlider = true, min = -1.5f, max = 1.5f, precision = 1000)
    public static float itemTranslationZ = 0f;

    @Comment(category = HAND, centered = true)
    public static Comment spacer4;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationX = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationY = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationZ = 0f;

    @Comment(category = ANIM)
    public static Comment animDesc;

    @Entry(category = ANIM)
    public static boolean enableAnimOverrides = false;

    @Entry(category = ANIM)
    public static boolean disableSwingBobbing = false;

    @Entry(category = ANIM)
    public static boolean ignoreSwingSpeedEffects = false;

    @Entry(category = ANIM, isSlider = true, min = 0.1f, max = 2f)
    public static float swingAnimationSpeed = 1f;

    @Entry(category = ANIM)
    public static boolean disableSwingAnimation = false;

    // ── View ────────────────────────────────────────────────────────────────

    // View (Crosshair + Camera)
    @Comment(category = VIEW)
    public static Comment viewDescription;

    @Entry(category = VIEW)
    public static boolean enableCrosshairInThirdPerson = false;

    @Entry(category = VIEW)
    public static boolean enableCrosshairInThirdPersonFront = false;

    @Entry(category = VIEW)
    public static boolean disableSelfieCam = false;
}