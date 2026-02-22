package com.github.kd_gaming1.scaleme.util;

import net.minecraft.world.InteractionHand;

/**
 * Shared context for the current first-person hand render pass.
 * Populated by {@code ItemInHandRendererMixin} before each item render and cleared after.
 */
public final class HandContext {

    /** {@code null} when outside a first-person hand render. */
    public static InteractionHand currentHand = null;

    /** Nesting depth of active hand render passes. */
    public static int depth = 0;

    /** Active transform values for the current hand render. */
    public static float tx, ty, tz;
    public static float rx, ry, rz;
    public static float s = 1f;

    private HandContext() {}
}