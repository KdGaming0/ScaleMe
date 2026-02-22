package com.github.kd_gaming1.scaleme;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScaleMe implements ClientModInitializer {
    public static final String MOD_ID = "scaleme";
    public static final Logger LOGGER = LoggerFactory.getLogger("Scale Me");

    @Override
    public void onInitializeClient() {
        MidnightConfig.init(MOD_ID, ScaleMeConfig.class);
    }
}