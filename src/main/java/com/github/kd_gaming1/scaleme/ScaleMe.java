package com.github.kd_gaming1.scaleme;

import com.github.kd_gaming1.scaleme.command.Commands;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.util.HypixelLocationState;
import eu.midnightdust.lib.config.MidnightConfig;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
//? if >=1.21.11 {
/*import net.minecraft.util.Util;
*///?} else {
import net.minecraft.Util;
//?}
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class ScaleMe implements ClientModInitializer {
    public static final String MOD_ID = "scaleme";
    public static final Logger LOGGER = LoggerFactory.getLogger("Scale Me");

    public static final AtomicBoolean hypixelPacketReceived = new AtomicBoolean(false);

    @Override
    public void onInitializeClient() {
        MidnightConfig.init(MOD_ID, ScaleMeConfig.class);

        HypixelLocationState.register();

        HypixelPacketEvents.HELLO.register((packet) -> {
            HypixelNetworking.registerToEvents(Util.make(new Object2IntOpenHashMap<>(), map -> map.put(LocationUpdateS2CPacket.ID, 1)));
            hypixelPacketReceived.set(true);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            hypixelPacketReceived.set(false);
            HypixelLocationState.reset();
        });

        // Register commands
        Commands.register();
    }
}