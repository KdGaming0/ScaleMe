package com.github.kd_gaming1.scaleme.util;

import com.github.kd_gaming1.scaleme.ScaleMe;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

public final class ScaleResolver {

    private ScaleResolver() {}

    /** Returns the configured scale for the given entity id, or 1.0f if no scaling applies. */
    public static float resolveScale(Minecraft mc, int entityId) {
        if (mc.player == null || mc.level == null) return 1f;

        boolean onHypixel = ScaleMe.hypixelPacketReceived.get();
        boolean scalingAllowed = !onHypixel || HypixelLocationState.isOnSkyblock();

        if (entityId == mc.player.getId()) {
            return scalingAllowed ? ScaleMeConfig.playerScale : 1f;
        }

        if (onHypixel && ScaleMeConfig.hypixelNpcScale != 1f) {
            var entity = mc.level.getEntity(entityId);
            if (entity instanceof AbstractClientPlayer player && HypixelNpcUtil.isHypixelNpc(player)) {
                return HypixelLocationState.isInDungeon() ? 1f : ScaleMeConfig.hypixelNpcScale;
            }
        }

        return scalingAllowed ? ScaleMeConfig.otherPlayersScale : 1f;
    }

    /** True when at least one scale config differs from 1.0f. */
    public static boolean noScalingConfigured() {
        return ScaleMeConfig.playerScale == 1f
                && ScaleMeConfig.otherPlayersScale == 1f
                && ScaleMeConfig.hypixelNpcScale == 1f
                && ScaleMeConfig.villagerNpcScale == 1f;
    }
}
