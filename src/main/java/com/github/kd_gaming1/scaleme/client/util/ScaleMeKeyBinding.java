package com.github.kd_gaming1.scaleme.client.util;

import com.github.kd_gaming1.scaleme.client.gui.ScaleMeManagerScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Handles key binding registration and GUI opening for ScaleMe mod.
 */
public class ScaleMeKeyBinding {
    private static KeyBinding openGuiKey;
    public static void register() {
        // Register the key binding
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "scaleme.key.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "scaleme.key.category"
        ));

        // Register client tick event to handle key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new ScaleMeManagerScreen());
                }
            }
        });
    }
}