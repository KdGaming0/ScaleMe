package com.github.kd_gaming1.scaleme.client.gui.components;

import com.github.kd_gaming1.scaleme.client.data.PlayerPreset;
import com.mojang.authlib.GameProfile;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PresetListEntryComponent extends FlowLayout {
    private static final int HEAD_SIZE = 24;
    private static final ConcurrentHashMap<UUID, SkinTextures> skinCache = new ConcurrentHashMap<>();

    private final PlayerPreset preset;
    private boolean isHovered = false;

    public PresetListEntryComponent(PlayerPreset preset) {
        super(Sizing.fill(100), Sizing.fixed(36), Algorithm.HORIZONTAL);

        this.preset = preset;

        // Set up the base styling
        this.surface(getSurfaceForState(preset.enabled, false))
                .padding(Insets.of(6))
                .verticalAlignment(VerticalAlignment.CENTER)
                .margins(Insets.vertical(2));

        // Add hover effects
        this.mouseEnter().subscribe(() -> {
            this.isHovered = true;
            this.surface(getSurfaceForState(preset.enabled, true));
        });

        this.mouseLeave().subscribe(() -> {
            this.isHovered = false;
            this.surface(getSurfaceForState(preset.enabled, false));
        });

        buildLayout();
    }

    private void buildLayout() {
        // Player head section
        FlowLayout headSection = (FlowLayout) Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .gap(0)
                .verticalAlignment(VerticalAlignment.CENTER);

        // Add player head with subtle border
        FlowLayout headContainer = (FlowLayout) Containers.verticalFlow(Sizing.fixed(HEAD_SIZE + 4), Sizing.fixed(HEAD_SIZE + 4))
                .surface(Surface.flat(0x44000000))
                .padding(Insets.of(2))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        headContainer.child(createPlayerHead(preset));
        headSection.child(headContainer);

        this.child(headSection);

        // Main content section
        FlowLayout contentSection = (FlowLayout) Containers.verticalFlow(Sizing.expand(), Sizing.content())
                .gap(2)
                .verticalAlignment(VerticalAlignment.CENTER);

        // Name row
        FlowLayout nameRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .gap(6)
                .verticalAlignment(VerticalAlignment.CENTER);

        nameRow.child(
                Components.label(Text.literal(preset.getEffectiveDisplayName()))
                        .color(getNameColor(preset.enabled))
                        .shadow(false)
                        .sizing(Sizing.expand(), Sizing.content())
        );

        // Status indicator
        nameRow.child(
                Components.label(Text.literal(preset.enabled ? "●" : "○"))
                        .color(preset.enabled ? Color.ofRgb(0x4CAF50) : Color.ofRgb(0x757575))
                        .sizing(Sizing.content(), Sizing.content())
        );

        contentSection.child(nameRow);

        // Scale info row
        FlowLayout scaleRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .gap(6)
                .verticalAlignment(VerticalAlignment.CENTER);

        scaleRow.child(
                Components.label(Text.literal("Scale: " + String.format("%.2f", preset.scale)))
                        .color(getScaleColor(preset))
                        .shadow(false)
                        .sizing(Sizing.content(), Sizing.content())
        );

        // Add a subtle scale indicator bar
        if (preset.enabled) {
            scaleRow.child(createScaleBar(preset.scale));
        }

        contentSection.child(scaleRow);
        this.child(contentSection);

        // Action section (right side)
        FlowLayout actionSection = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                .gap(2)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);

        // Status text
        actionSection.child(
                Components.label(Text.literal(preset.enabled ? "ACTIVE" : "DISABLED"))
                        .color(preset.enabled ? Color.ofRgb(0x4CAF50) : Color.ofRgb(0xFF9800))
                        .shadow(false)
                        .sizing(Sizing.content(), Sizing.content())
        );

        this.child(actionSection);
    }

    private FlowLayout createScaleBar(float scale) {
        int barWidth = 40;
        int barHeight = 3;

        // Map scale 0.1 (empty) to 3.0 (full)
        float normalizedScale = Math.max(0, Math.min(1, (scale - 0.1f) / (3.0f - 0.1f)));
        int fillWidth = (int)(barWidth * normalizedScale);

        FlowLayout scaleBar = (FlowLayout) Containers.horizontalFlow(Sizing.fixed(barWidth), Sizing.fixed(barHeight))
                .surface(Surface.flat(0x33FFFFFF));

        if (fillWidth > 0) {
            scaleBar.child(
                    Components.box(Sizing.fixed(fillWidth), Sizing.fixed(barHeight))
                            .color(getScaleBarColor(scale))
            );
        }

        return scaleBar;
    }

    private Color getScaleBarColor(float scale) {
        if (scale < 0.8f) return Color.ofRgb(0x2196F3); // Blue for small
        if (scale > 1.5f) return Color.ofRgb(0xFF5722); // Red for large
        return Color.ofRgb(0x4CAF50); // Green for normal
    }

    private Surface getSurfaceForState(boolean enabled, boolean hovered) {
        if (hovered) {
            return enabled ?
                    Surface.flat(0x44424242).and(Surface.outline(0xFF4CAF50)) :
                    Surface.flat(0x44424242).and(Surface.outline(0xFF757575));
        } else {
            return enabled ?
                    Surface.flat(0x33424242) :
                    Surface.flat(0x22424242);
        }
    }

    private Color getNameColor(boolean enabled) {
        return enabled ? Color.WHITE : Color.ofRgb(0xBBBBBB);
    }

    private Color getScaleColor(PlayerPreset preset) {
        if (!preset.enabled) return Color.ofRgb(0x888888);

        if (preset.scale < 0.8f) return Color.ofRgb(0x81D4FA); // Light blue
        if (preset.scale > 1.5f) return Color.ofRgb(0xFFAB91); // Light red
        return Color.ofRgb(0xA5D6A7); // Light green
    }

    private TextureComponent createPlayerHead(PlayerPreset preset) {
        SkinTextures skinTextures = getSkinTextures(preset);
        Identifier skinTexture = skinTextures.texture();

        return (TextureComponent) Components.texture(skinTexture, 8, 8, 8, 8, 64, 64)
                .sizing(Sizing.fixed(HEAD_SIZE), Sizing.fixed(HEAD_SIZE));
    }

    private SkinTextures getSkinTextures(PlayerPreset preset) {
        try {
            if (preset.isUUID()) {
                UUID playerUUID = UUID.fromString(preset.identifier);

                // Check cache first
                SkinTextures cached = skinCache.get(playerUUID);
                if (cached != null) {
                    return cached;
                }

                // Try to get from player list (for online players)
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.getNetworkHandler() != null) {
                    PlayerListEntry playerListEntry = client.getNetworkHandler().getPlayerListEntry(playerUUID);
                    if (playerListEntry != null) {
                        SkinTextures textures = playerListEntry.getSkinTextures();
                        skinCache.put(playerUUID, textures);
                        return textures;
                    }
                }

                // Fallback: Use default skin and try to fetch async
                SkinTextures defaultTextures = DefaultSkinHelper.getSkinTextures(playerUUID);
                skinCache.put(playerUUID, defaultTextures);

                // Try to fetch the actual skin asynchronously
                fetchSkinAsync(playerUUID, preset.getEffectiveDisplayName());

                return defaultTextures;
            }
        } catch (IllegalArgumentException e) {
            // Invalid UUID, fall through to default
        }

        // Default steve/alex skin for non-UUID presets or invalid UUIDs
        return DefaultSkinHelper.getSkinTextures(UUID.randomUUID());
    }

    private void fetchSkinAsync(UUID playerUUID, String playerName) {
        Util.getMainWorkerExecutor().execute(() -> {
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                GameProfile profile = new GameProfile(playerUUID, playerName);

                // Fetch complete profile
                var completeProfile = client.getSessionService().fetchProfile(playerUUID, false).profile();
                if (completeProfile != null) {
                    // Fetch skin textures
                    client.getSkinProvider().fetchSkinTextures(completeProfile).thenAccept(optionalTextures -> {
                        optionalTextures.ifPresent(textures -> {
                            // Update cache with real textures
                            skinCache.put(playerUUID, textures);
                            // Note: You might want to trigger a UI refresh here if needed
                        });
                    });
                }
            } catch (Exception e) {
                // Ignore errors in async skin fetching
            }
        });
    }
}