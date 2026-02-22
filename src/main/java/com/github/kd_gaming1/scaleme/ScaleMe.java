package com.github.kd_gaming1.scaleme;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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

        HypixelPacketEvents.HELLO.register((packet) -> hypixelPacketReceived.set(true));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> hypixelPacketReceived.set(false));

    }
}