package com.github.kd_gaming1.scaleme.client.gui;

import com.github.kd_gaming1.scaleme.client.data.PlayerPreset;
import com.github.kd_gaming1.scaleme.client.gui.components.PresetEditorComponent;
import com.github.kd_gaming1.scaleme.client.gui.components.PresetListEntryComponent;
import com.github.kd_gaming1.scaleme.client.util.PlayerPresetManager;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import eu.midnightdust.lib.config.MidnightConfig;
import com.github.kd_gaming1.scaleme.Scaleme;

import java.util.ArrayList;
import java.util.List;

public class ScaleMeManagerScreen extends BaseOwoScreen<FlowLayout> {
    private FlowLayout presetList;
    private ScrollContainer<FlowLayout> scrollContainer;
    private PresetEditorComponent editorComponent;
    private final List<PresetListEntryComponent> entryComponents = new ArrayList<>();

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER);
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);

        // Title section
        FlowLayout titleSection = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.content())
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .margins(Insets.of(10));

        titleSection.child(
                Components.label(Text.of("ScaleMe Manager"))
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .color(Color.WHITE)
                        .shadow(true)
        );
        titleSection.child(
                Components.label(Text.of("Player-Specific Scaling"))
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .color(Color.ofRgb(0xAAFFAA))
                        .shadow(false)
                        .margins(Insets.top(2))
        );
        titleSection.child(
                Components.label(Text.of("Use 'Config' for personal and global scaling settings"))
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .color(Color.ofRgb(0x888888))
                        .shadow(false)
                        .margins(Insets.top(2))
        );
        rootComponent.child(titleSection);

        // Main split layout
        FlowLayout splitRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(65))
                .gap(12)
                .margins(Insets.horizontal(10))
                .horizontalAlignment(HorizontalAlignment.CENTER);

        FlowLayout leftColumn = Containers.verticalFlow(Sizing.fill(49), Sizing.fill(100));
        FlowLayout rightColumn = Containers.verticalFlow(Sizing.fill(49), Sizing.fill(100));
        splitRow.child(leftColumn);
        splitRow.child(rightColumn);
        rootComponent.child(splitRow);

        buildLeftColumn(leftColumn);
        buildRightColumn(rightColumn);

        // Bottom button row
        FlowLayout buttonRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(90), Sizing.content())
                .gap(8)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .margins(Insets.of(15));

        buttonRow.child(Components.button(Text.literal("New Preset"), button -> editorComponent.createNewPreset()));
        buttonRow.child(Components.button(Text.literal("Config"), button -> {
            if (client.player != null) {
                client.setScreen(MidnightConfig.getScreen(client.currentScreen, Scaleme.MOD_ID));
            }
        }));
        buttonRow.child(Components.button(Text.literal("Refresh"), button -> refreshPresetList()));
        buttonRow.child(Components.button(Text.literal("Done"), button -> this.close()));

        rootComponent.child(buttonRow);
    }

    private void buildLeftColumn(FlowLayout leftColumn) {
        leftColumn.child(
                Components.label(Text.literal("Presets"))
                        .color(Color.WHITE)
                        .shadow(true)
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .margins(Insets.of(2, 8, 2, 2))
        );

        this.presetList = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.content())
                .padding(Insets.of(2));
        this.scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), this.presetList);
        leftColumn.child(this.scrollContainer);

        refreshPresetList();
    }

    private void buildRightColumn(FlowLayout rightColumn) {
        rightColumn.child(
                Components.label(Text.literal("Editor"))
                        .color(Color.WHITE)
                        .shadow(true)
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .margins(Insets.of(2, 8, 2, 2))
        );

        this.editorComponent = new PresetEditorComponent(
                this::onPresetUpdated,
                this::onPresetDeleted
        );
        rightColumn.child(this.editorComponent);
    }

    private void refreshPresetList() {
        this.presetList.clearChildren();
        this.entryComponents.clear();

        for (PlayerPreset preset : PlayerPresetManager.getAllPresets()) {
            PresetListEntryComponent entry = new PresetListEntryComponent(preset);
            entry.mouseDown().subscribe((mouseX, mouseY, button) -> {
                if (button == 0) {
                    selectPreset(preset, entry);
                    return true;
                }
                return false;
            });
            this.entryComponents.add(entry);
            this.presetList.child(entry);
        }
    }

    private void selectPreset(PlayerPreset preset, PresetListEntryComponent entryComponent) {
        editorComponent.editPreset(preset);
    }

    private void onPresetUpdated(PlayerPreset preset) {
        refreshPresetList();
    }

    private void onPresetDeleted(PlayerPreset preset) {
        refreshPresetList();
    }
}