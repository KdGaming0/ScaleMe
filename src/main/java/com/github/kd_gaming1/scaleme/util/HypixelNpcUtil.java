package com.github.kd_gaming1.scaleme.util;

import com.github.kd_gaming1.scaleme.ScaleMe;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team.Visibility;

/**
 * Utility for detecting Hypixel NPCs.
 * <p>
 * Detection method: Hypixel NPCs are fake player entities whose scoreboard team
 * has name tag visibility set to {@link Visibility#NEVER} to hide their tags.
 * Gated on {@code hypixelPacketReceived} so this is a no-op on other servers.
 */
public class HypixelNpcUtil {

    private HypixelNpcUtil() {}

    /**
     * Returns true if the entity is a Hypixel NPC.
     * Always returns false if not connected to Hypixel.
     */
    public static boolean isHypixelNpc(AbstractClientPlayer player) {
        if (!ScaleMe.hypixelPacketReceived.get()) return false;
        if (player == null) return false;

        PlayerTeam team = player.getTeam();
        if (team == null) return false;

        return team.getNameTagVisibility() == Visibility.NEVER;
    }
}