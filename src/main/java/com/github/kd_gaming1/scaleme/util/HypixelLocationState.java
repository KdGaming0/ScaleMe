package com.github.kd_gaming1.scaleme.util;

import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;

public class HypixelLocationState {

    private static boolean onSkyblock = false;
    private static boolean inDungeon = false;

    private HypixelLocationState() {}

    public static void register() {
        HypixelPacketEvents.LOCATION_UPDATE.register(packet -> {
            if (packet instanceof LocationUpdateS2CPacket location) {
                onSkyblock = location.serverType()
                        .map(type -> type.equals("SKYBLOCK"))
                        .orElse(false);

                inDungeon = onSkyblock && location.map()
                        .map(map -> map.equals("Dungeon"))
                        .orElse(false);
            }
        });
    }

    /** True if the player is on Hypixel SkyBlock (any island/mode). */
    public static boolean isOnSkyblock() { return onSkyblock; }

    /** True if the player is in a SkyBlock Dungeon specifically. */
    public static boolean isInDungeon() { return inDungeon; }

    public static void reset() {
        onSkyblock = false;
        inDungeon = false;
    }
}