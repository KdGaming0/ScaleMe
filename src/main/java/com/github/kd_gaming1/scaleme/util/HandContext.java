package com.github.kd_gaming1.scaleme.util;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.world.InteractionHand;

/** Holds the resolved transform for whichever hand is currently rendering. Only valid while renderDepth > 0. */
public final class HandContext {

    public static InteractionHand currentHand = null;
    public static int renderDepth = 0;

    public static float translationX, translationY, translationZ;
    public static float rotationX, rotationY, rotationZ;
    public static float scale = 1f;

    private HandContext() {}

    /** Resolves config values for the given hand. Off-hand uses its own sliders only when separately enabled. */
    public static void update(InteractionHand hand) {
        currentHand = hand;

        boolean useOffhandTransform = (hand == InteractionHand.OFF_HAND)
                && ScaleMeConfig.enableSeparateHandTransforms;

        if (useOffhandTransform) {
            translationX = ScaleMeConfig.itemTranslationXOffhand;
            translationY = ScaleMeConfig.itemTranslationYOffhand;
            translationZ = ScaleMeConfig.itemTranslationZOffhand;
            rotationX    = ScaleMeConfig.itemRotationXOffhand;
            rotationY    = ScaleMeConfig.itemRotationYOffhand;
            rotationZ    = ScaleMeConfig.itemRotationZOffhand;
            scale        = ScaleMeConfig.itemScaleOffhand;
        } else {
            translationX = ScaleMeConfig.itemTranslationX;
            translationY = ScaleMeConfig.itemTranslationY;
            translationZ = ScaleMeConfig.itemTranslationZ;
            rotationX    = ScaleMeConfig.itemRotationX;
            rotationY    = ScaleMeConfig.itemRotationY;
            rotationZ    = ScaleMeConfig.itemRotationZ;
            scale        = ScaleMeConfig.itemScale;
        }
    }

    public static boolean hasActiveTransform() {
        return translationX != 0f || translationY != 0f || translationZ != 0f
                || rotationX    != 0f || rotationY    != 0f || rotationZ    != 0f
                || scale        != 1f;
    }
}