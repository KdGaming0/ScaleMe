package com.github.kd_gaming1.scaleme.util;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import net.minecraft.client.Minecraft;

public final class PerTickCache {

    private PerTickCache() {}

    private static final Int2FloatOpenHashMap cache = new Int2FloatOpenHashMap();
    private static long lastTick = Long.MIN_VALUE;
    private static Object lastLevel = null;

    static {
        cache.defaultReturnValue(Float.NaN);
    }

    /** Returns the cached scale for entityId, computing it once per tick. */
    public static float getScale(int entityId) {
        if (!FeatureFlags.isEnabled(FeatureFlags.SCALE_ANY)) return 1f;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 1f;

        Object level = mc.level;
        long tick = mc.level.getGameTime();

        if (level != lastLevel || tick != lastTick) {
            cache.clear();
            lastLevel = level;
            lastTick = tick;
        }

        float cached = cache.get(entityId);
        if (!Float.isNaN(cached)) return cached;

        float scale = ScaleResolver.resolveScale(mc, entityId);
        cache.put(entityId, scale);
        return scale;
    }
}
