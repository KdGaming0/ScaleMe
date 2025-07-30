package com.github.kd_gaming1.scaleme;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scaleme implements ModInitializer {
    public static final String MOD_ID = "scaleme";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        MidnightConfig.init(MOD_ID, ScaleMeConfig.class);
    }
}