package com.github.kd_gaming1.scaleme.client;

import com.github.kd_gaming1.scaleme.Scaleme;
import com.github.kd_gaming1.scaleme.client.command.PresetCommand;
import com.github.kd_gaming1.scaleme.client.util.ScaleManager;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ScalemeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MidnightConfig.init(Scaleme.MOD_ID, ScaleMeConfig.class);

        ScaleManager.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> ScaleManager.tick());

        PresetCommand.register();
    }

}