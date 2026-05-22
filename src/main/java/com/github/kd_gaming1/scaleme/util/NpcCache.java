package com.github.kd_gaming1.scaleme.util;

import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * Per-tick cache for Hypixel NPC detection to avoid repeated scoreboard lookups.
 */
public final class NpcCache {

    private static final Int2BooleanOpenHashMap CACHE = new Int2BooleanOpenHashMap();
    private static long lastTick = Long.MIN_VALUE;

    static {
        CACHE.defaultReturnValue(false);
    }

    private NpcCache() {}

    /**
     * Returns true if the entity with the given ID is a Hypixel NPC.
     * Result is cached for the current tick to avoid redundant lookups.
     */
    public static boolean isHypixelNpc(int entityId) {
        if (!HypixelLocationState.isOnHypixel()) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;

        long tick = mc.level.getGameTime();
        if (tick != lastTick) {
            CACHE.clear();
            lastTick = tick;
        }

        if (CACHE.containsKey(entityId)) {
            return CACHE.get(entityId);
        }

        boolean result = compute(mc, entityId);
        CACHE.put(entityId, result);
        return result;
    }

    private static boolean compute(Minecraft mc, int entityId) {
        var entity = mc.level.getEntity(entityId);
        if (!(entity instanceof AbstractClientPlayer player)) return false;
        return HypixelNpcUtil.isHypixelNpc(player);
    }
}
