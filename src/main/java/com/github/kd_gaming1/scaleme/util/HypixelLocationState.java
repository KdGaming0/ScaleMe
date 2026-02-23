package com.github.kd_gaming1.scaleme.util;

import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;

public final class HypixelLocationState {

    private static boolean onSkyblock = false;
    private static boolean inDungeon  = false;

    private HypixelLocationState() {}

    public static void register() {
        HypixelPacketEvents.LOCATION_UPDATE.register(packet -> {
            if (!(packet instanceof LocationUpdateS2CPacket location)) return;

            onSkyblock = location.serverType()
                    .map("SKYBLOCK"::equals)
                    .orElse(false);

            // Dungeon is a sub-mode of SkyBlock, so short-circuit when not on SkyBlock
            inDungeon = onSkyblock && location.map()
                    .map("Dungeon"::equals)
                    .orElse(false);
        });
    }

    public static boolean isOnSkyblock() { return onSkyblock; }
    public static boolean isInDungeon()  { return inDungeon;  }

    public static void reset() {
        onSkyblock = false;
        inDungeon  = false;
    }
}