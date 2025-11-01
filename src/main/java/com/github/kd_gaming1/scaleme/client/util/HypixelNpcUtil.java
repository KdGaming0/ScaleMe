package com.github.kd_gaming1.scaleme.client.util;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.AbstractTeam;

/**
 * Utility for detecting Hypixel NPCs.
 * <p>
 * NPCs on Hypixel are identified by their scoreboard team having name tag visibility
 * set to NEVER, which is used to hide their name tags from players.
 */
public class HypixelNpcUtil {

    /**
     * Checks if the given player entity is a Hypixel NPC.
     * <p>
     * Detection method: NPCs on Hypixel have their scoreboard team's
     * name tag visibility rule set to {@link AbstractTeam.VisibilityRule#NEVER}.
     *
     * @param player The player entity to check
     * @return true if this entity is a Hypixel NPC, false otherwise
     */
    public static boolean isHypixelNpc(AbstractClientPlayerEntity player) {
        if (player == null) {
            return false;
        }

        Team team = player.getScoreboardTeam();
        if (team == null) {
            return false;
        }

        AbstractTeam.VisibilityRule nameTagRule = team.getNameTagVisibilityRule();
        return nameTagRule == AbstractTeam.VisibilityRule.NEVER;
    }
}