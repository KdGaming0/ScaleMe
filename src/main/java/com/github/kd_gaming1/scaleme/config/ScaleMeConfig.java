package com.github.kd_gaming1.scaleme.config;

import eu.midnightdust.lib.config.MidnightConfig;

/**
 * Config model for ScaleMe.
 * <p>
 * Structure rules, so the menu stays readable:
 * <ul>
 *   <li>No "master" toggles. Every toggle turns on exactly one feature.</li>
 *   <li>Sliders whose "off" state is a value (scales) have no toggle — 1.0 means off.</li>
 *   <li>Sliders whose "off" state is not a value (vanilla anchors/arcs are non-zero)
 *       get one toggle, and their dependents are hidden with {@link Condition}
 *       rather than documented in the tooltip text.</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class ScaleMeConfig extends MidnightConfig {
    public static final String HAND = "hand_item";
    public static final String ANIM = "animation";
    public static final String SCALE = "scale";
    public static final String VIEW = "view";

    // ════ Held Item ═════════════════════════════════════════════════════════

    @Comment(category = HAND)
    public static Comment handDesc;

    // ── Hand Position ───────────────────────────────────────────────────────

    @Comment(category = HAND)
    public static Comment armPosDesc;

    @Entry(category = HAND)
    public static boolean enableArmPositionOverride = false;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    @Condition(requiredOption = "enableArmPositionOverride", requiredValue = "true")
    public static float armBaseX = 0.56f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    @Condition(requiredOption = "enableArmPositionOverride", requiredValue = "true")
    public static float armBaseY = -0.52f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    @Condition(requiredOption = "enableArmPositionOverride", requiredValue = "true")
    public static float armBaseZ = -0.72f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 0f, precision = 1000)
    @Condition(requiredOption = "enableArmPositionOverride", requiredValue = "true")
    public static float armHeightScale = -0.6f;

    // ── Item Size & Angle ───────────────────────────────────────────────────

    @Comment(category = HAND, centered = true)
    public static Comment spacer1;

    @Comment(category = HAND)
    public static Comment itemTransformDesc;

    @Entry(category = HAND)
    public static boolean enableItemTransformOverride = false;

    @Entry(category = HAND, isSlider = true, min = 0.1f, max = 3f, precision = 1000)
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemScale = 1f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemTranslationX = 0f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemTranslationY = 0f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemTranslationZ = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemRotationX = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemRotationY = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemRotationZ = 0f;

    // ── Off-Hand ────────────────────────────────────────────────────────────

    @Comment(category = HAND, centered = true)
    public static Comment spacer2;

    @Comment(category = HAND)
    public static Comment offhandDesc;

    @Entry(category = HAND)
    public static boolean enableSeparateHandTransforms = false;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableArmPositionOverride", requiredValue = "true")
    public static float armBaseXOffhand = 0.56f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableArmPositionOverride", requiredValue = "true")
    public static float armBaseYOffhand = -0.52f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 2f, precision = 1000)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableArmPositionOverride", requiredValue = "true")
    public static float armBaseZOffhand = -0.72f;

    @Entry(category = HAND, isSlider = true, min = -2f, max = 0f, precision = 1000)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableArmPositionOverride", requiredValue = "true")
    public static float armHeightScaleOffhand = -0.6f;

    @Entry(category = HAND, isSlider = true, min = 0.1f, max = 3f, precision = 1000)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemScaleOffhand = 1f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemTranslationXOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemTranslationYOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -1f, max = 1f, precision = 1000)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemTranslationZOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemRotationXOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemRotationYOffhand = 0f;

    @Entry(category = HAND, isSlider = true, min = -180f, max = 180f, precision = 10)
    @Condition(requiredOption = "enableSeparateHandTransforms", requiredValue = "true")
    @Condition(requiredOption = "enableItemTransformOverride", requiredValue = "true")
    public static float itemRotationZOffhand = 0f;

    // ════ Swing Animation ═══════════════════════════════════════════════════

    @Comment(category = ANIM)
    public static Comment animDesc;

    @Entry(category = ANIM)
    public static boolean enableSwordBlock = false;

    // ── Swing Behaviour ─────────────────────────────────────────────────────

    @Comment(category = ANIM, centered = true)
    public static Comment spacer3;

    @Comment(category = ANIM)
    public static Comment swingBasicsDesc;

    @Entry(category = ANIM)
    public static boolean disableSwingAnimation = false;

    @Entry(category = ANIM, isSlider = true, min = 0.1f, max = 2f)
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static float swingAnimationSpeed = 1f;

    @Entry(category = ANIM)
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static boolean ignoreSwingSpeedEffects = false;

    @Entry(category = ANIM)
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static boolean disableSwingBobbing = false;

    // ── Swing Shape ─────────────────────────────────────────────────────────

    @Comment(category = ANIM, centered = true)
    public static Comment spacer4;

    @Comment(category = ANIM)
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static Comment swingShapeDesc;

    @Entry(category = ANIM)
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static boolean enableSwingOverride = false;

    @Entry(category = ANIM, isSlider = true, min = -180f, max = 180f, precision = 10)
    @Condition(requiredOption = "enableSwingOverride", requiredValue = "true")
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static float swingArcXAmount = -80f;   // vanilla: -80

    @Entry(category = ANIM, isSlider = true, min = -180f, max = 180f, precision = 10)
    @Condition(requiredOption = "enableSwingOverride", requiredValue = "true")
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static float swingArcYAmount = -20f;   // vanilla: -20

    @Entry(category = ANIM, isSlider = true, min = -180f, max = 180f, precision = 10)
    @Condition(requiredOption = "enableSwingOverride", requiredValue = "true")
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static float swingArcZAmount = -20f;   // vanilla: -20

    @Entry(category = ANIM, isSlider = true, min = 0f, max = 90f, precision = 10)
    @Condition(requiredOption = "enableSwingOverride", requiredValue = "true")
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static float swingPreRotationY = 45f;  // vanilla: 45

    @Entry(category = ANIM)
    @Condition(requiredOption = "enableSwingOverride", requiredValue = "true")
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static boolean swingCounterRotation = true;

    @Entry(category = ANIM, isSlider = true, min = -2f, max = 2f)
    @Condition(requiredOption = "enableSwingOverride", requiredValue = "true")
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static float swingArmXScale = -0.4f;   // vanilla: -0.4

    @Entry(category = ANIM, isSlider = true, min = -2f, max = 2f)
    @Condition(requiredOption = "enableSwingOverride", requiredValue = "true")
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static float swingArmYScale = 0.2f;    // vanilla: 0.2

    @Entry(category = ANIM, isSlider = true, min = -2f, max = 2f)
    @Condition(requiredOption = "enableSwingOverride", requiredValue = "true")
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static float swingArmZScale = -0.2f;   // vanilla: -0.2

    @Entry(category = ANIM)
    @Condition(requiredOption = "enableSwingOverride", requiredValue = "true")
    @Condition(requiredOption = "disableSwingAnimation", requiredValue = "false")
    public static boolean swingArmXMultiplyBySide = true;

    // ════ Sizes ═════════════════════════════════════════════════════════════
    // Every slider here is independent and self-gating: 1.0 = off.

    @Comment(category = SCALE)
    public static Comment scaleDesc;

    @Entry(category = SCALE, isSlider = true, min = 0.1f, max = 4f)
    public static float playerScale = 1f;

    @Entry(category = SCALE, isSlider = true, min = 0.1f, max = 4f)
    public static float otherPlayersScale = 1f;

    @Entry(category = SCALE, isSlider = true, min = 0.1f, max = 4f)
    public static float villagerNpcScale = 1f;

    @Entry(category = SCALE, isSlider = true, min = 0.1f, max = 4f)
    public static float hypixelNpcScale = 1f;

    @Comment(category = SCALE, centered = true)
    public static Comment spacer5;

    @Entry(category = SCALE)
    public static boolean scaleNameTags = false;

    @Comment(category = SCALE, centered = true)
    public static Comment spacer6;

    @Comment(category = SCALE)
    public static Comment groundItemDesc;

    @Entry(category = SCALE, isSlider = true, min = 0.1f, max = 4f, precision = 1000)
    public static float groundItemScale = 1f;

    // ════ Camera & Crosshair ════════════════════════════════════════════════

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

    @Comment(category = VIEW, centered = true)
    public static Comment spacer7;

    @Entry(category = VIEW)
    public static boolean hidePlayers = false;

    @Entry(category = VIEW)
    @Condition(requiredOption = "hidePlayers", requiredValue = "true")
    public static boolean hidePlayersOnlyOnSkyblock = false;
}
