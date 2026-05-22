package com.github.kd_gaming1.scaleme.util;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

public final class ScaleResolver {

    private ScaleResolver() {}

    /** Returns the configured scale for the given entity id, or 1.0f if no scaling applies. */
    public static float resolveScale(Minecraft mc, int entityId) {
        if (mc.player == null || mc.level == null) return 1f;

        boolean onHypixel = HypixelLocationState.isOnHypixel();
        boolean scalingAllowed = !onHypixel || HypixelLocationState.isOnSkyblock();

        if (entityId == mc.player.getId()) {
            return ScaleMeConfig.playerScale;
        }

        if (onHypixel) {
            var entity = mc.level.getEntity(entityId);
            if (entity instanceof AbstractClientPlayer player && HypixelNpcUtil.isHypixelNpc(player)) {
                return HypixelLocationState.isInDungeon() ? 1f : ScaleMeConfig.hypixelNpcScale;
            }
        }

        return scalingAllowed ? ScaleMeConfig.otherPlayersScale : 1f;
    }

    /** True when at least one scale config differs from 1.0f. */
    public static boolean noScalingConfigured() {
        return !FeatureFlags.isEnabled(FeatureFlags.SCALE_ANY);
    }
}
