package com.github.kd_gaming1.scaleme;

import com.github.kd_gaming1.scaleme.command.Commands;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.util.BlockingState;
import com.github.kd_gaming1.scaleme.util.FeatureFlags;
import com.github.kd_gaming1.scaleme.util.HypixelLocationState;
import com.github.kd_gaming1.scaleme.util.SwingHoldState;
import eu.midnightdust.lib.config.MidnightConfig;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.util.Util;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.tags.ItemTags;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScaleMe implements ClientModInitializer {

    public static final String MOD_ID = "scaleme";
    public static final Logger LOGGER = LoggerFactory.getLogger("Scale Me");

    @Override
    public void onInitializeClient() {
        MidnightConfig.init(MOD_ID, ScaleMeConfig.class);

        HypixelNetworking.registerToEvents(
                Util.make(new Object2IntOpenHashMap<>(), map -> map.put(LocationUpdateS2CPacket.ID, 1))
        );

        HypixelLocationState.register();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            HypixelLocationState.reset();
            SwingHoldState.reset();
        });

        KeyMapping blockKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.scaleme.sword_block",
                InputConstants.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                KeyMapping.Category.GAMEPLAY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FeatureFlags.update();

            if (client.player == null) {
                SwingHoldState.reset();
                return;
            }

            SwingHoldState.update(FeatureFlags.isEnabled(FeatureFlags.SUPPRESS_REPEAT_SWING)
                    && client.options.keyAttack.isDown());

            BlockingState.isBlocking = FeatureFlags.isEnabled(FeatureFlags.SWORD_BLOCK)
                    && blockKey.isDown()
                    && client.player.getMainHandItem().is(ItemTags.SWORDS);
        });

        Commands.register();
    }
}
