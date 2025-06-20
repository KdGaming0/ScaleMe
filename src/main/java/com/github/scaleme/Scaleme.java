package com.github.scaleme;

import com.github.scaleme.config.ScaleMeConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;

public class Scaleme implements ModInitializer {
    public static final String MOD_ID = "scaleme";

    @Override
    public void onInitialize() {
        MidnightConfig.init(MOD_ID, ScaleMeConfig.class);
    }
}