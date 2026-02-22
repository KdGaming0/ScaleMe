package com.github.kd_gaming1.scaleme.config;

import eu.midnightdust.lib.config.MidnightConfig;

@SuppressWarnings("unused")
public class ScaleMeConfig extends MidnightConfig {
    public static final String HAND = "hand_item";
    public static final String ANIM = "animation";
    public static final String VIEW = "view";

    // ── Hand Item Transform ─────────────────────────────────────────────────

    @Comment(category = HAND)
    public static Comment handDesc;

    @Entry(category = HAND)
    public static boolean enableHandItemTransform = false;

    @Entry(category = HAND)
    public static boolean enableSeparateHandTransforms = false;

    @Comment(category = HAND, centered = true)
    public static Comment spacer1;

    @Entry(category = HAND, name = "Scale", isSlider = true, min = 0.1f, max = 4f, precision = 1000)
    public static float itemScale = 1f;

    @Entry(category = HAND, isSlider = true, min = -1.5f, max = 1.5f, precision = 1000)
    public static float itemTranslationX = 0f;

    @Entry(category = HAND, isSlider = true, min = -1.5f, max = 1.5f, precision = 1000)
    public static float itemTranslationY = 0f;

    @Entry(category = HAND, isSlider = true, min = -1.5f, max = 1.5f, precision = 1000)
    public static float itemTranslationZ = 0f;

    @Comment(category = HAND, centered = true)
    public static Comment spacer2;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationX = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationY = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationZ = 0f;

    // ── Offhand Item Transform ──────────────────────────────────────────────

    @Comment(category = HAND, centered = true)
    public static Comment spacer3;

    @Comment(category = HAND)
    public static Comment offhandDesc;

    @Comment(category = HAND, centered = true)
    public static Comment spacer4;

    @Entry(category = HAND, isSlider = true, min = 0.1f, max = 4f, precision = 1000)
    public static float itemScaleOffhand = 1f;

    @Entry(category = HAND, isSlider = true, min = -1.5f, max = 1.5f, precision = 1000)
    public static float itemTranslationXOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -1.5f, max = 1.5f, precision = 1000)
    public static float itemTranslationYOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -1.5f, max = 1.5f, precision = 1000)
    public static float itemTranslationZOffhand = 0f;

    @Comment(category = HAND, centered = true)
    public static Comment spacer5;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationXOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationYOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationZOffhand = 0f;

    // ── Animation ───────────────────────────────────────────────────────────

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

    @Comment(category = VIEW)
    public static Comment viewDescription;

    @Entry(category = VIEW)
    public static boolean enableCrosshairInThirdPerson = false;

    @Entry(category = VIEW)
    public static boolean enableCrosshairInThirdPersonFront = false;

    @Entry(category = VIEW)
    public static boolean disableSelfieCam = false;

    @Entry(category = VIEW)
    public static boolean showOwnNametagInThirdPerson = false;
}