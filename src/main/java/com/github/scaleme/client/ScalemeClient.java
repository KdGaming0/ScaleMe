package com.github.scaleme.client;

import com.github.scaleme.Scaleme;
import com.github.scaleme.client.util.ScaleManager;
import com.github.scaleme.config.ScaleMeConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ScalemeClient implements ClientModInitializer {

    private static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        MidnightConfig.init(Scaleme.MOD_ID, ScaleMeConfig.class);

        // Register keybinding for opening config
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.scaleme.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.scaleme.general"
        ));

        // Handle key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(MidnightConfig.getScreen(client.currentScreen, Scaleme.MOD_ID));
                }
            }
        });

        ScaleManager.init();
    }
}