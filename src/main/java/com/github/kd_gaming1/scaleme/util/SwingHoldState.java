package com.github.kd_gaming1.scaleme.util;

/** Tracks whether a held attack input has already started its visible swing. */
public final class SwingHoldState {

    private static boolean firstSwingSeen;
    private static boolean suppressing;
    private static boolean bottomReached;

    private SwingHoldState() {}

    /** Clears the hold when attack is released or the feature is disabled. */
    public static void update(boolean holdingAttack) {
        if (!holdingAttack) reset();
    }

    /**
     * Records a swing for the current attack hold.
     *
     * @return {@code true} when this and all later swings in the hold should be hidden
     */
    public static boolean onSwing(boolean holdingAttack) {
        if (!holdingAttack) {
            reset();
            return false;
        }

        if (firstSwingSeen) {
            suppressing = true;
            return true;
        }

        firstSwingSeen = true;
        return false;
    }

    public static boolean isSuppressing() {
        return suppressing;
    }

    /** Latches once the first swing reaches its downward peak and holds until release. */
    public static boolean latchBottomIfReached(float swingProgress, float bottomProgress) {
        if (suppressing && swingProgress >= bottomProgress) bottomReached = true;
        return bottomReached;
    }

    public static void reset() {
        firstSwingSeen = false;
        suppressing = false;
        bottomReached = false;
    }
}
