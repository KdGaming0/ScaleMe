package com.github.kd_gaming1.scaleme.client.util;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.AbstractTeam;

public class HypixelNpcUtil {
    /**
     * Checks if the given player entity is a Hypixel NPC by inspecting its scoreboard team.
     * NPCs on Hypixel typically have their name tag visibility set to NEVER.
     *
     * @param player The player entity to check.
     * @return true if this entity is a Hypixel NPC, false otherwise.
     */
    public static boolean isHypixelNpc(AbstractClientPlayerEntity player) {
        if (player == null) return false;
        Team team = player.getScoreboardTeam();
        if (team == null) return false;

        // Hypixel NPC detection: name tag visibility == NEVER
        AbstractTeam.VisibilityRule rule = team.getNameTagVisibilityRule();
        return rule == AbstractTeam.VisibilityRule.NEVER;
    }
}