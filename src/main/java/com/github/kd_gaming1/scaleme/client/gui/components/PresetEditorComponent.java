package com.github.kd_gaming1.scaleme.client.gui.components;

import com.github.kd_gaming1.scaleme.client.data.PlayerPreset;
import com.github.kd_gaming1.scaleme.client.util.PlayerPresetManager;
import com.github.kd_gaming1.scaleme.client.util.PlayerUUIDResolver;
import com.github.kd_gaming1.scaleme.client.util.ScaleConstants;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.SlimSliderComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;
import java.util.function.Consumer;

public class PresetEditorComponent extends FlowLayout {
    private static final float MIN_SCALE = 0.1f;
    private static final float MAX_SCALE = 3.0f;

    private PlayerPreset currentPreset;
    private Consumer<PlayerPreset> onPresetUpdated;
    private Consumer<PlayerPreset> onPresetDeleted;
    private boolean isNewPreset = false;

    // Form components
    private TextBoxComponent identifierField;
    private TextBoxComponent friendlyNameField;
    private SlimSliderComponent scaleSlider;
    private LabelComponent scaleValueLabel;
    private ButtonComponent saveButton;
    private ButtonComponent deleteButton;
    private ButtonComponent enableToggleButton;
    private ButtonComponent cancelButton;

    // UI components
    private LabelComponent titleLabel;
    private LabelComponent statusLabel;
    private LabelComponent identifierInfoLabel;
    private FlowLayout emptyStateContainer;
    private ScrollContainer<FlowLayout> editorContainer;
    private FlowLayout helpContainer;
    private boolean isShowingEmptyState = true;

    // Validation state
    private boolean isIdentifierValid = true;
    private boolean hasUnsavedChanges = false;
    private String originalIdentifier = null;

    public PresetEditorComponent(Consumer<PlayerPreset> onPresetUpdated, Consumer<PlayerPreset> onPresetDeleted) {
        super(Sizing.fill(100), Sizing.fill(100), Algorithm.VERTICAL);
        this.onPresetUpdated = onPresetUpdated;
        this.onPresetDeleted = onPresetDeleted;
        this.surface(Surface.flat(0x33424242)).padding(Insets.of(8));
        buildLayout();
        showEmptyState();
    }

    // --- Layout Building ---

    private void buildLayout() {
        this.titleLabel = Components.label(Text.literal("Select a Preset"))
                .color(Color.WHITE)
                .shadow(true)
                .horizontalTextAlignment(HorizontalAlignment.CENTER);
        this.child(this.titleLabel);

        this.statusLabel = (LabelComponent) Components.label(Text.empty())
                .color(Color.ofRgb(0xCCCCCC))
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .margins(Insets.bottom(8));
        this.child(this.statusLabel);

        buildEmptyState();
        buildEditorContainer();
    }

    private void buildEmptyState() {
        this.emptyStateContainer = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.expand())
                .gap(12)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        this.emptyStateContainer.child(
                Components.label(Text.literal("📋"))
                        .color(Color.ofRgb(0x888888))
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .sizing(Sizing.content(), Sizing.content())
        );
        this.emptyStateContainer.child(
                Components.label(Text.literal("Select a preset to edit"))
                        .color(Color.ofRgb(0xCCCCCC))
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .sizing(Sizing.content(), Sizing.content())
        );
        this.emptyStateContainer.child(
                Components.label(Text.literal("or create a new one"))
                        .color(Color.ofRgb(0x888888))
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .sizing(Sizing.content(), Sizing.content())
        );
        this.child(this.emptyStateContainer);
    }

    private void buildEditorContainer() {
        FlowLayout mainContent = Containers.verticalFlow(Sizing.fill(95), Sizing.content()).gap(8);
        buildHelpContainer();

        FlowLayout formContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content()).gap(8);

        // Player Identifier Field
        formContainer.child(createFieldSection(
                "Player Identifier: *",
                "Username (will be resolved automatically)",
                "Enter the exact username of the player you want to scale",
                identifier -> {
                    this.identifierField = identifier;
                    identifier.onChanged().subscribe(this::onIdentifierChanged);
                }
        ));

        // Display Name Field
        formContainer.child(createFieldSection(
                "Display Name:",
                "Custom name (optional)",
                "Leave empty to use the player identifier as display name",
                friendlyName -> {
                    this.friendlyNameField = friendlyName;
                    friendlyName.onChanged().subscribe(this::onFriendlyNameChanged);
                }
        ));

        // Scale Slider Section
        formContainer.child(createScaleSliderSection());

        this.identifierInfoLabel = (LabelComponent) Components.label(Text.empty())
                .color(Color.ofRgb(0x888888))
                .shadow(false)
                .sizing(Sizing.fill(100), Sizing.content());
        formContainer.child(this.identifierInfoLabel);

        mainContent.child(formContainer);
        buildButtonSection(mainContent);

        this.editorContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.expand(), mainContent);
    }

    private FlowLayout createScaleSliderSection() {
        FlowLayout section = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.content())
                .surface(Surface.flat(0x22000000))
                .padding(Insets.of(8));

        // Label row with current value
        FlowLayout labelRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .gap(4)
                .verticalAlignment(VerticalAlignment.CENTER);

        labelRow.child(
                Components.label(Text.literal("Scale: *"))
                        .color(Color.ofRgb(0xFFAB91))
                        .shadow(false)
        );

        this.scaleValueLabel = (LabelComponent) Components.label(Text.literal("1.00"))
                .color(Color.ofRgb(0x4CAF50))
                .shadow(false);
        labelRow.child(this.scaleValueLabel);

        section.child(labelRow);

        // SlimSliderComponent - more minimal look
        this.scaleSlider = Components.slimSlider(SlimSliderComponent.Axis.HORIZONTAL);
        this.scaleSlider.sizing(Sizing.fill(100), Sizing.content());
        this.scaleSlider.min(0.0);
        this.scaleSlider.max(1.0);
        this.scaleSlider.stepSize(0.01);
        this.scaleSlider.tooltipSupplier(value -> Text.literal(String.format("%.2f", mapSliderToScale(value))));
        this.scaleSlider.onChanged().subscribe(this::onScaleSliderChanged);

        section.child(this.scaleSlider);

        // Scale indicators
        FlowLayout indicatorRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .gap(4)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .margins(Insets.top(4));

        indicatorRow.child(Components.label(Text.literal("Size multiplier for the player:")).color(Color.ofRgb(0x888888)));
        indicatorRow.child(Components.label(Text.literal("Tiny")).color(Color.ofRgb(0x888888)));
        indicatorRow.child(Components.label(Text.literal("(0.1)")).color(Color.ofRgb(0x666666)));
        indicatorRow.child(Components.label(Text.literal("Normal")).color(Color.ofRgb(0x4CAF50)));
        indicatorRow.child(Components.label(Text.literal("(1.0)")).color(Color.ofRgb(0x4CAF50)));
        indicatorRow.child(Components.label(Text.literal("Large")).color(Color.ofRgb(0xFF5722)));
        indicatorRow.child(Components.label(Text.literal("(3.0)")).color(Color.ofRgb(0xFF5722)));

        section.child(indicatorRow);

        return section;
    }

    // Scale mapping functions
    private double mapScaleToSlider(float scale) {
        return (scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE);
    }

    private float mapSliderToScale(double sliderValue) {
        float scale = (float) (MIN_SCALE + sliderValue * (MAX_SCALE - MIN_SCALE));
        return Math.round(scale * 100f) / 100f; // Round to 2 decimal places
    }

    private void buildHelpContainer() {
        this.helpContainer = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.content())
                .surface(Surface.flat(0x22004488))
                .padding(Insets.of(8))
                .margins(Insets.bottom(8));

        FlowLayout headerRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .gap(6)
                .verticalAlignment(VerticalAlignment.CENTER);

        headerRow.child(
                Components.label(Text.literal("ℹ"))
                        .color(Color.ofRgb(0x64B5F6))
                        .sizing(Sizing.content(), Sizing.content())
        );
        headerRow.child(
                Components.label(Text.literal("Creating New Preset"))
                        .color(Color.ofRgb(0x64B5F6))
                        .shadow(false)
                        .sizing(Sizing.content(), Sizing.content())
        );

        this.helpContainer.child(headerRow);
        this.helpContainer.child(
                Components.label(Text.literal("• Enter a username (will be resolved to UUID automatically)"))
                        .color(Color.ofRgb(0xE3F2FD))
                        .shadow(false)
                        .sizing(Sizing.content(), Sizing.content())
        );
        this.helpContainer.child(
                Components.label(Text.literal("• Friendly name will auto-fill if left empty"))
                        .color(Color.ofRgb(0xE3F2FD))
                        .shadow(false)
                        .sizing(Sizing.content(), Sizing.content())
        );
        this.helpContainer.child(
                Components.label(Text.literal("• Use the slider to set the desired scale"))
                        .color(Color.ofRgb(0xE3F2FD))
                        .shadow(false)
                        .sizing(Sizing.content(), Sizing.content())
        );
    }

    private void buildButtonSection(FlowLayout contentContainer) {
        FlowLayout buttonSection = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.content())
                .surface(Surface.flat(0x22000000))
                .padding(Insets.of(8));

        FlowLayout buttonRow = Containers.ltrTextFlow(Sizing.fill(100), Sizing.content()).gap(6);

        this.saveButton = (ButtonComponent) Components.button(Text.literal("Save"), button -> saveCurrentPreset())
                .horizontalSizing(Sizing.fixed(120));
        buttonRow.child(this.saveButton);

        this.cancelButton = (ButtonComponent) Components.button(Text.literal("Cancel"), button -> cancelEditing())
                .horizontalSizing(Sizing.fixed(80));
        buttonRow.child(this.cancelButton);

        this.enableToggleButton = (ButtonComponent) Components.button(Text.literal("Enable"), button -> togglePresetEnabled())
                .horizontalSizing(Sizing.fixed(80));
        buttonRow.child(this.enableToggleButton);

        this.deleteButton = (ButtonComponent) Components.button(Text.literal("Delete"), button -> deleteCurrentPreset())
                .horizontalSizing(Sizing.fixed(80));
        buttonRow.child(this.deleteButton);

        buttonSection.child(buttonRow);
        contentContainer.child(buttonSection);
    }

    private FlowLayout createFieldSection(String labelText, String placeholder, String helpText, Consumer<TextBoxComponent> fieldConsumer) {
        FlowLayout section = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.content())
                .surface(Surface.flat(0x22000000))
                .padding(Insets.of(8));

        FlowLayout labelRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .gap(4)
                .verticalAlignment(VerticalAlignment.CENTER);

        labelRow.child(
                Components.label(Text.literal(labelText))
                        .color(labelText.contains("*") ? Color.ofRgb(0xFFAB91) : Color.WHITE)
                        .shadow(false)
        );
        section.child(labelRow);

        TextBoxComponent textField = Components.textBox(Sizing.fill(100));
        if (placeholder != null && !placeholder.isEmpty()) {
            textField.setSuggestion(placeholder);
            textField.onChanged().subscribe(text -> textField.setSuggestion(text.isEmpty() ? placeholder : ""));
        }

        fieldConsumer.accept(textField);
        section.child(textField);

        if (helpText != null && !helpText.isEmpty()) {
            section.child(
                    Components.label(Text.literal(helpText))
                            .color(Color.ofRgb(0x888888))
                            .shadow(false)
                            .sizing(Sizing.fill(100), Sizing.content())
            );
        }
        return section;
    }

    // --- State Management ---

    public void editPreset(PlayerPreset preset) {
        if (preset == null) {
            showEmptyState();
            return;
        }
        this.currentPreset = preset.copy(); // Use a copy for editing
        this.hasUnsavedChanges = false;
        this.isNewPreset = false;
        this.originalIdentifier = preset.identifier;

        if (this.identifierField != null) {
            this.identifierField.active = false;
            this.identifierField.setEditable(false);
        }

        populateFields(this.currentPreset);
        updateButtonStates();
        updateTitle();
        updateStatusLabel();
        showEditorState();
    }

    public void createNewPreset() {
        PlayerPreset newPreset = new PlayerPreset("", "", 1.0f);
        newPreset.enabled = true;
        this.isNewPreset = true;
        this.currentPreset = newPreset;
        this.hasUnsavedChanges = false;
        this.originalIdentifier = null;

        if (this.identifierField != null) {
            this.identifierField.active = true;
            this.identifierField.setEditable(true);
            this.identifierField.setEditableColor(0xFFFFFFFF); // Reset to normal color
        }

        populateFields(newPreset);
        updateButtonStates();
        updateTitle();
        updateStatusLabel();
        showEditorState();
    }

    private void cancelEditing() {
        this.currentPreset = null;
        this.isNewPreset = false;
        this.hasUnsavedChanges = false;
        this.originalIdentifier = null;
        showEmptyState();
    }

    private void updateTitle() {
        if (this.currentPreset == null) {
            this.titleLabel.text(Text.literal("Select a Preset"));
        } else if (this.isNewPreset) {
            this.titleLabel.text(Text.literal("Creating New Preset").formatted(Formatting.GREEN));
        } else {
            this.titleLabel.text(Text.literal("Editing: " + this.currentPreset.getEffectiveDisplayName()));
        }
    }

    private void updateStatusLabel() {
        if (this.currentPreset == null) {
            this.statusLabel.text(Text.empty());
        } else if (this.isNewPreset) {
            this.statusLabel.text(Text.literal("Fill in the required fields to create a new preset").formatted(Formatting.YELLOW));
        } else {
            String status = this.currentPreset.enabled ? "Active" : "Disabled";
            this.statusLabel.text(Text.literal("Status: " + status));
        }
    }

    private void populateFields(PlayerPreset preset) {
        this.identifierField.text(preset.identifier != null ? preset.identifier : "");
        this.friendlyNameField.text(preset.friendlyName != null ? preset.friendlyName : "");

        // Set slider value
        double sliderValue = mapScaleToSlider(preset.scale);
        this.scaleSlider.value(sliderValue);
        updateScaleValueLabel(preset.scale);

        validateIdentifierFormat(preset.identifier);
    }

    private void showEmptyState() {
        if (!this.isShowingEmptyState) {
            this.removeChild(this.editorContainer);
            this.child(this.emptyStateContainer);
            this.isShowingEmptyState = true;
        }
        updateTitle();
        updateStatusLabel();
    }

    private void showEditorState() {
        if (this.isShowingEmptyState) {
            this.removeChild(this.emptyStateContainer);
            this.child(this.editorContainer);
            this.isShowingEmptyState = false;
        }

        FlowLayout mainContent = this.editorContainer.child();
        if (this.isNewPreset && !mainContent.children().contains(this.helpContainer)) {
            mainContent.child(0, this.helpContainer);
        } else if (!this.isNewPreset && mainContent.children().contains(this.helpContainer)) {
            mainContent.removeChild(this.helpContainer);
        }

        // Activate fields and set proper colors
        if (this.identifierField != null) {
            this.identifierField.active = true;
            if (this.isNewPreset) {
                this.identifierField.setEditableColor(0xFFFFFFFF); // Normal white color
            }
        }
        if (this.friendlyNameField != null) this.friendlyNameField.active = true;
        // Remove the slider.active line - SlimSliderComponent doesn't have this property

        if (this.isNewPreset) {
            this.identifierInfoLabel.text(Text.literal("Enter a username (player must be online for UUID resolution).").formatted(Formatting.YELLOW));
        } else {
            this.identifierInfoLabel.text(Text.literal("Identifier is read-only when editing a preset.").formatted(Formatting.GRAY));
        }
    }

    // --- Validation and Helpers ---

    private void validateField(TextBoxComponent field, boolean isValid, int validColor, int invalidColor) {
        if (field != null) {
            field.setEditableColor(isValid ? validColor : invalidColor);
        }
    }

    private void validateIdentifierFormat(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            this.isIdentifierValid = false;
            if (this.isNewPreset) {
                validateField(this.identifierField, false, 0xFFFFFFFF, 0xFFEB1D36);
                this.statusLabel.text(Text.literal("Player identifier is required").formatted(Formatting.RED));
            }
            return;
        }

        String trimmed = identifier.trim();

        // Check for valid UUID format
        boolean isUUIDFormat = isValidUUID(trimmed);

        // Check for valid username format (3-16 chars, alphanumeric + underscore)
        boolean isUsernameFormat = trimmed.matches(ScaleConstants.USERNAME_REGEX);

        if (this.isNewPreset) {
            this.isIdentifierValid = isUsernameFormat;

            String message;
            Formatting color;

            if (isUsernameFormat) {
                message = "Username format valid - will resolve when saved";
                color = Formatting.YELLOW;
            } else if (trimmed.length() < 3) {
                message = "Username too short (minimum 3 characters)";
                color = Formatting.RED;
            } else if (trimmed.length() > 16) {
                message = "Username too long (maximum 16 characters)";
                color = Formatting.RED;
            } else {
                message = "Invalid characters - use only letters, numbers, and underscores";
                color = Formatting.RED;
            }

            this.statusLabel.text(Text.literal(message).formatted(color));
            validateField(this.identifierField, this.isIdentifierValid, 0xFFFFFFFF, 0xFFEB1D36);
        } else {
            this.isIdentifierValid = isUUIDFormat || isUsernameFormat;
            this.statusLabel.text(Text.literal(isUUIDFormat ? "Valid UUID format" : "Username format")
                    .formatted(Formatting.GREEN));
        }
    }

    private boolean isValidUUID(String input) {
        try {
            UUID.fromString(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void updateScaleValueLabel(float scale) {
        String scaleText = String.format("%.2f", scale);
        Color scaleColor;

        if (scale < 0.8f) {
            scaleColor = Color.ofRgb(0x81D4FA); // Light blue for small
        } else if (scale > 1.5f) {
            scaleColor = Color.ofRgb(0xFFAB91); // Light red for large
        } else {
            scaleColor = Color.ofRgb(0xA5D6A7); // Light green for normal
        }

        this.scaleValueLabel.text(Text.literal(scaleText)).color(scaleColor);
    }

    // --- Event Handlers ---

    private void onIdentifierChanged(String newValue) {
        if (!this.isNewPreset) return;

        // Sanitize input: remove spaces and invalid characters
        String sanitized = newValue.replaceAll("[^a-zA-Z0-9_]", "");
        if (!sanitized.equals(newValue)) {
            this.identifierField.text(sanitized);
            return; // Will trigger this method again with sanitized value
        }

        validateIdentifierFormat(sanitized);
        this.hasUnsavedChanges = true;
        updateButtonStates();
    }

    private void onFriendlyNameChanged(String newValue) {
        if (this.currentPreset == null) return;
        if (!newValue.equals(this.currentPreset.friendlyName)) {
            this.hasUnsavedChanges = true;
            this.currentPreset.friendlyName = newValue.isEmpty() ? null : newValue;
        }
        updateButtonStates();
    }

    private void onScaleSliderChanged(double sliderValue) {
        if (this.currentPreset == null) return;

        float newScale = mapSliderToScale(sliderValue);
        if (Math.abs(newScale - this.currentPreset.scale) > 0.001f) {
            this.hasUnsavedChanges = true;
            this.currentPreset.scale = newScale;
            updateScaleValueLabel(newScale); // This was missing!
        }
        updateButtonStates();
    }

    // --- Button Actions ---

    private void updateButtonStates() {
        boolean hasValidPreset = this.currentPreset != null;
        boolean canSave = hasValidPreset && this.isIdentifierValid &&
                (!this.identifierField.getText().trim().isEmpty());

        this.saveButton.active = canSave;
        this.cancelButton.active = true;
        this.enableToggleButton.active = !this.isNewPreset && hasValidPreset;
        this.deleteButton.active = !this.isNewPreset && hasValidPreset;

        if (!this.isNewPreset && hasValidPreset) {
            this.enableToggleButton.setMessage(Text.literal(this.currentPreset.enabled ? "Disable" : "Enable"));
        }

        if (this.isNewPreset) {
            this.saveButton.setMessage(Text.literal("Create Preset"));
        } else if (this.hasUnsavedChanges) {
            this.saveButton.setMessage(Text.literal("Save Changes"));
        } else {
            this.saveButton.setMessage(Text.literal("Save"));
        }
    }

    private void saveCurrentPreset() {
        if (this.currentPreset == null) return;

        // Validate the preset before saving
        try {
            this.currentPreset.validateForSave();
        } catch (IllegalArgumentException e) {
            showErrorMessage("Cannot save: " + e.getMessage());
            return;
        }

        if (!this.isIdentifierValid) {
            showErrorMessage("Please provide a valid player identifier");
            return;
        }

        if (this.isNewPreset) {
            String inputIdentifier = this.identifierField.getText().trim();

            // Additional validation for username format
            if (!inputIdentifier.matches(ScaleConstants.USERNAME_REGEX)) {
                showErrorMessage("Invalid username format. Use 3-16 alphanumeric characters or underscores.");
                return;
            }

            // Resolve UUID from username
            UUID resolvedUUID = PlayerUUIDResolver.resolvePlayerUUID(inputIdentifier);
            if (resolvedUUID == null) {
                showErrorMessage("Failed to resolve player: " + inputIdentifier + ". Player must be online or check the username.");
                return;
            }

            // Set resolved UUID and auto-fill friendly name if empty
            this.currentPreset.identifier = resolvedUUID.toString();
            if (this.currentPreset.friendlyName == null || this.currentPreset.friendlyName.trim().isEmpty()) {
                this.currentPreset.friendlyName = inputIdentifier;
                this.friendlyNameField.text(inputIdentifier);
            }
        }

        // Final validation before saving
        if (!this.currentPreset.isValidForSave()) {
            showErrorMessage("Preset contains invalid data and cannot be saved");
            return;
        }

        // Save the preset
        PlayerPresetManager.addOrUpdatePreset(this.currentPreset);

        // Switch to edit mode after successful creation
        if (this.isNewPreset) {
            this.isNewPreset = false;
            this.identifierField.active = false;
            this.identifierField.setEditable(false);
            this.identifierField.text(this.currentPreset.identifier);
        }

        this.hasUnsavedChanges = false;
        this.originalIdentifier = this.currentPreset.identifier;

        updateButtonStates();
        updateTitle();
        updateStatusLabel();
        showEditorState();

        if (this.onPresetUpdated != null) {
            this.onPresetUpdated.accept(this.currentPreset);
        }

        showSuccessMessage("Preset saved successfully!");
    }

    private void togglePresetEnabled() {
        if (this.currentPreset == null || this.isNewPreset) return;

        this.currentPreset.enabled = !this.currentPreset.enabled;
        PlayerPresetManager.setPresetEnabled(this.currentPreset.identifier, this.currentPreset.enabled);

        updateButtonStates();
        updateStatusLabel();

        if (this.onPresetUpdated != null) {
            this.onPresetUpdated.accept(this.currentPreset);
        }

        showSuccessMessage("Preset " + (this.currentPreset.enabled ? "enabled" : "disabled") + "!");
    }

    private void deleteCurrentPreset() {
        if (this.currentPreset == null || this.isNewPreset) return;

        String presetName = this.currentPreset.getEffectiveDisplayName();
        showConfirmationDialog(
                "Delete Preset",
                "Are you sure you want to delete the preset '" + presetName + "'?\nThis action cannot be undone.",
                () -> {
                    PlayerPresetManager.removePreset(this.currentPreset.identifier);
                    if (this.onPresetDeleted != null) {
                        this.onPresetDeleted.accept(this.currentPreset);
                    }
                    this.currentPreset = null;
                    showEmptyState();
                    showSuccessMessage("Preset deleted!");
                },
                null
        );
    }

    // --- Dialogs and Messages ---

    private void showConfirmationDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        FlowLayout overlay = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100))
                .surface(Surface.flat(0x88000000))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout dialog = (FlowLayout) Containers.verticalFlow(Sizing.fixed(320), Sizing.content())
                .surface(Surface.flat(0xFF424242).and(Surface.outline(0xFF666666)))
                .padding(Insets.of(16))
                .horizontalAlignment(HorizontalAlignment.CENTER);

        dialog.child(
                Components.label(Text.literal(title))
                        .color(Color.WHITE)
                        .shadow(true)
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .margins(Insets.bottom(4))
        );

        String[] messageLines = message.split("\n");
        FlowLayout messageContainer = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.content())
                .gap(2)
                .horizontalAlignment(HorizontalAlignment.CENTER);

        for (String line : messageLines) {
            messageContainer.child(
                    Components.label(Text.literal(line))
                            .color(Color.ofRgb(0xCCCCCC))
                            .horizontalTextAlignment(HorizontalAlignment.CENTER)
            );
        }
        dialog.child(messageContainer);

        FlowLayout buttonRow = (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .gap(8)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .margins(Insets.top(8));

        buttonRow.child(Components.button(Text.literal("Cancel"), button -> {
            this.removeChild(overlay);
            if (onCancel != null) onCancel.run();
        }).horizontalSizing(Sizing.fixed(80)));

        buttonRow.child(Components.button(Text.literal("Confirm"), button -> {
            this.removeChild(overlay);
            if (onConfirm != null) onConfirm.run();
        }).horizontalSizing(Sizing.fixed(80)));

        dialog.child(buttonRow);
        overlay.child(dialog);
        this.child(overlay);
    }

    private void showErrorMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§c" + message), false);
        }
        this.statusLabel.text(Text.literal(message).formatted(Formatting.RED));
    }

    private void showSuccessMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§a" + message), false);
        }
    }
}