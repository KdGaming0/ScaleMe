package com.github.kd_gaming1.scaleme.config;

import eu.midnightdust.lib.config.MidnightConfig;

@SuppressWarnings("unused")
public class ScaleMeConfig extends MidnightConfig {
    public static final String HAND = "hand_item";
    public static final String ANIM = "animation";
    public static final String SCALE = "scale";
    public static final String VIEW = "view";
    public static final String ITEM = "item";

    // ── Hand Item Transform ─────────────────────────────────────────────────

    @Comment(category = HAND)
    public static Comment handDesc;

    @Entry(category = HAND)
    public static boolean enableHandItemTransform = false;

    // ── Arm Base Position ───────────────────────────────────────────────────

    @Comment(category = HAND, centered = true)
    public static Comment spacer1;

    @Comment(category = HAND)
    public static Comment armPosDesc;

    @Entry(category = HAND)
    public static boolean enableArmPositionOverride = false;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    public static float armBaseX = 0.56f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    public static float armBaseY = -0.52f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    public static float armBaseZ = -0.72f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 0f, precision = 1000)
    public static float armHeightScale = -0.6f;

    // ── Item Transform (Main Hand) ──────────────────────────────────────────

    @Comment(category = HAND, centered = true)
    public static Comment spacer2;

    @Comment(category = HAND)
    public static Comment itemTransformDesc;

    @Entry(category = HAND)
    public static boolean enableItemTransformOverride = false;

    @Entry(category = HAND, name = "Scale", isSlider = true, min = 0.1f, max = 3f, precision = 1000)
    public static float itemScale = 1f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    public static float itemTranslationX = 0f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    public static float itemTranslationY = 0f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    public static float itemTranslationZ = 0f;

    @Comment(category = HAND, centered = true)
    public static Comment spacer3;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationX = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationY = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float itemRotationZ = 0f;

    // ── Off-Hand Transform ──────────────────────────────────────────────────

    @Comment(category = HAND, centered = true)
    public static Comment spacer4;

    @Comment(category = HAND)
    public static Comment offhandDesc;

    @Entry(category = HAND)
    public static boolean enableSeparateHandTransforms = false;

    @Comment(category = HAND)
    public static Comment armOffhandDesc;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    public static float armBaseXOffhand = 0.56f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    public static float armBaseYOffhand = -0.52f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    public static float armBaseZOffhand = -0.72f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 0f, precision = 1000)
    public static float armHeightScaleOffhand = -0.6f;

    @Comment(category = HAND, centered = true)
    public static Comment spacer5;

    @Entry(category = HAND, isSlider = true, min = 0.1f, max = 3f, precision = 1000)
    public static float itemScaleOffhand = 1f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    public static float itemTranslationXOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    public static float itemTranslationYOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    public static float itemTranslationZOffhand = 0f;

    @Comment(category = HAND, centered = true)
    public static Comment spacer6;

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
    public static boolean enableSwordBlock = false;

    @Comment(category = HAND, centered = true)
    public static Comment spacer7;

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

    @Comment(category = ANIM, centered = true)
    public static Comment spacer8;

    @Entry(category = ANIM)
    public static boolean enableSwingOverride = false;

    @Entry(category = ANIM, isSlider = true, min = -2f, max = 2f)
    public static float swingArmXScale = -0.4f;   // vanilla: -0.4

    @Entry(category = ANIM, isSlider = true, min = -2f, max = 2f)
    public static float swingArmYScale = 0.2f;    // vanilla: 0.2

    @Entry(category = ANIM, isSlider = true, min = -2f, max = 2f)
    public static float swingArmZScale = -0.2f;   // vanilla: -0.2

    @Entry(category = ANIM)
    public static boolean swingArmXMultiplyBySide = true;

    @Entry(category = ANIM, isSlider = true, min = 0f, max = 90f, precision = 10)
    public static float swingPreRotationY = 45f;  // vanilla: 45

    @Entry(category = ANIM, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float swingArcYAmount = -20f;   // vanilla: -20

    @Entry(category = ANIM, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float swingArcZAmount = -20f;   // vanilla: -20

    @Entry(category = ANIM, isSlider = true, min = -180f, max = 180f, precision = 10)
    public static float swingArcXAmount = -80f;   // vanilla: -80

    @Entry(category = ANIM)
    public static boolean swingCounterRotation = true;

    // ── Scale ───────────────────────────────────────────────────────────────

    @Comment(category = SCALE)
    public static Comment scaleDesc;

    @Entry(category = SCALE)
    public static boolean scaleNameTags = false;

    @Entry(category = SCALE, isSlider = true, min = 0.1f, max = 4f)
    public static float playerScale = 1f;

    @Entry(category = SCALE, isSlider = true, min = 0.1f, max = 4f)
    public static float otherPlayersScale = 1f;

    @Entry(category = SCALE, isSlider = true, min = 0.1f, max = 4f)
    public static float villagerNpcScale = 1f;

    @Entry(category = SCALE, isSlider = true, min = 0.1f, max = 4f)
    public static float hypixelNpcScale = 1f;

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

    @Entry(category = VIEW)
    public static boolean hidePlayers = false;

    @Entry(category = VIEW)
    public static boolean hidePlayersOnlyOnSkyblock = false;

    // ── Item Entity (Ground) ────────────────────────────────────────────────

    @Comment(category = ITEM)
    public static Comment itemDesc;

    @Entry(category = ITEM)
    public static boolean enableGroundItemScale = false;

    @Entry(category = ITEM, isSlider = true, min = 0.1f, max = 4f, precision = 1000)
    public static float groundItemScale = 1f;
}