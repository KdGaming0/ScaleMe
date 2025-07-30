package com.github.kd_gaming1.scaleme.client.util;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import com.github.kd_gaming1.scaleme.Scaleme;

/**
 * Handles key binding registration and GUI opening for ScaleMe mod.
 */
public class ScaleMeKeyBinding {
    private static KeyBinding openGuiKey;
    private static KeyBinding openConfigKey;

    public static void register() {
        // Register the key binding
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.scaleme.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "scaleme.key.category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Config menu
            while (openConfigKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(MidnightConfig.getScreen(client.currentScreen, Scaleme.MOD_ID));
                }
            }
        });
    }
}