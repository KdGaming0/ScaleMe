package com.github.scaleme.client.gui;

import com.github.scaleme.client.data.PlayerPreset;
import com.github.scaleme.client.util.PlayerPresetManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class AddEditPresetScreen extends Screen {
    private final Screen parent;
    private final PlayerPreset editingPreset;
    private final boolean isEditing;

    private TextFieldWidget displayNameField;
    private TextFieldWidget identifierField;
    private CategoryDropdownWidget categoryDropdown;
    private CheckboxWidget enabledCheckbox;
    private ScaleSliderWidget scaleSlider;
    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;

    // For 'Add New Category' dialog
    private boolean creatingCategory = false;
    private TextFieldWidget newCategoryField;
    private ButtonWidget saveNewCategoryButton;
    private ButtonWidget cancelNewCategoryButton;

    // Tooltip tracking
    private int tooltipX, tooltipY;
    private Text tooltipText;

    // Layout constants
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 75;
    private static final int ELEMENT_SPACING = 4;
    private static final int FIELD_TO_FIELD_SPACING = 30;
    private static final int LABEL_TO_FIELD_SPACING = 6;
    private static final int LABEL_OFFSET = 5;
    private static final int CHECKBOX_SPACING = 25;
    private static final int BUTTON_SPACING = 30;

    public AddEditPresetScreen(Screen parent, PlayerPreset preset) {
        super(Text.translatable(preset == null ?
                "scaleme.gui.presets.add_custom.title" :
                "scaleme.gui.presets.edit_custom.title")
        );
        this.parent = parent;
        this.editingPreset = preset;
        this.isEditing = preset != null;
    }

    @Override
    protected void init() {
        super.init();

        final int centerX = this.width / 2;
        final int startY = 40;
        final int fieldX = centerX - FIELD_WIDTH / 2;

        int currentY = startY;

        // Identifier field (required)
        currentY += LABEL_TO_FIELD_SPACING;
        this.identifierField = new TextFieldWidget(
                this.textRenderer,
                fieldX, currentY,
                FIELD_WIDTH, FIELD_HEIGHT,
                Text.translatable("scaleme.gui.preset.identifier")
        );
        this.identifierField.setPlaceholder(
                Text.translatable("scaleme.gui.preset.identifier.placeholder").formatted(Formatting.GRAY)
        );
        this.identifierField.setMaxLength(100);
        if (isEditing) this.identifierField.setText(editingPreset.identifier);
        this.addDrawableChild(this.identifierField);
        currentY += FIELD_TO_FIELD_SPACING;

        // Display Name field (optional)
        currentY += LABEL_TO_FIELD_SPACING;
        this.displayNameField = new TextFieldWidget(
                this.textRenderer,
                fieldX, currentY,
                FIELD_WIDTH, FIELD_HEIGHT,
                Text.translatable("scaleme.gui.preset.display_name")
        );
        this.displayNameField.setPlaceholder(
                Text.translatable("scaleme.gui.preset.display_name.placeholder").formatted(Formatting.GRAY)
        );
        this.displayNameField.setMaxLength(50);
        if (isEditing && editingPreset.displayName != null) {
            this.displayNameField.setText(editingPreset.displayName);
        }
        this.addDrawableChild(this.displayNameField);
        currentY += FIELD_TO_FIELD_SPACING;

        // Category dropdown
        currentY += LABEL_TO_FIELD_SPACING;
        this.categoryDropdown = new CategoryDropdownWidget(
                fieldX, currentY, FIELD_WIDTH, FIELD_HEIGHT,
                isEditing ? editingPreset.category : "default"
        );
        this.addDrawableChild(this.categoryDropdown);
        currentY += FIELD_TO_FIELD_SPACING;

        // Scale Slider
        currentY += LABEL_TO_FIELD_SPACING;
        float initialScale = isEditing ? editingPreset.scale : 1.0f;
        this.scaleSlider = new ScaleSliderWidget(
                fieldX, currentY,
                FIELD_WIDTH, FIELD_HEIGHT,
                initialScale
        );
        this.addDrawableChild(this.scaleSlider);
        currentY += CHECKBOX_SPACING;

        // Enabled checkbox
        this.enabledCheckbox = CheckboxWidget.builder(
                        Text.translatable("scaleme.gui.preset.enabled"),
                        this.textRenderer
                )
                .pos(fieldX, currentY)
                .checked(isEditing ? editingPreset.enabled : true)
                .build();
        this.addDrawableChild(this.enabledCheckbox);
        currentY += BUTTON_SPACING;

        // Action buttons
        final int totalButtonWidth = (BUTTON_WIDTH * 2) + ELEMENT_SPACING;
        final int buttonStartX = centerX - (totalButtonWidth / 2);

        this.saveButton = ButtonWidget.builder(
                Text.translatable("scaleme.gui.preset.save"),
                button -> savePreset()
        ).dimensions(buttonStartX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.saveButton);

        this.cancelButton = ButtonWidget.builder(
                ScreenTexts.CANCEL,
                button -> this.client.setScreen(parent)
        ).dimensions(buttonStartX + BUTTON_WIDTH + ELEMENT_SPACING, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.cancelButton);

        // Set initial focus
        this.setInitialFocus(this.identifierField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Update save button state
        boolean canSave = !this.identifierField.getText().trim().isEmpty();
        this.saveButton.active = canSave && !this.creatingCategory;

        // Render main screen elements only if the dialog is not open
        if (!this.creatingCategory) {
            // Title
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    this.title,
                    this.width / 2,
                    20,
                    0xFFFFFF
            );

            // Field labels
            renderFieldLabels(context);

            // Handle tooltips
            handleTooltips(mouseX, mouseY);
        }

        // Render dropdown LAST and at a higher Z-index to ensure it's on top
        if (categoryDropdown != null && categoryDropdown.isOpen()) {
            context.getMatrices().push();
            context.getMatrices().translate(0.0, 0.0, 400.0);
            categoryDropdown.renderDropdown(context, mouseX, mouseY);
            context.getMatrices().pop();
        }

        // Render tooltip if needed (and dropdown is not open)
        if (tooltipText != null && (categoryDropdown == null || !categoryDropdown.isOpen()) && !this.creatingCategory) {
            context.drawTooltip(this.textRenderer, tooltipText, tooltipX, tooltipY);
        }

        // Render 'Add New Category' dialog if active, ensuring it's on top of everything
        if (this.creatingCategory) {
            context.getMatrices().push();
            context.getMatrices().translate(0.0, 0.0, 500.0);
            renderNewCategoryDialog(context, mouseX, mouseY, delta);
            context.getMatrices().pop();
        }
    }

    private void renderNewCategoryDialog(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim background
        context.fill(0, 0, this.width, this.height, 0x80000000);

        // Draw dialog box
        final int dialogWidth = 160;
        final int dialogHeight = 90;
        final int dialogX = (this.width - dialogWidth) / 2;
        final int dialogY = (this.height - dialogHeight) / 2;

        context.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, 0xFF1E1E1E);
        context.drawBorder(dialogX, dialogY, dialogWidth, dialogHeight, 0xFF666666);

        // Draw title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("scaleme.gui.category.new.title"),
                this.width / 2,
                dialogY + 10,
                0xFFFFFF
        );

        // Re-render dialog widgets on top of the overlay
        this.newCategoryField.render(context, mouseX, mouseY, delta);
        this.saveNewCategoryButton.render(context, mouseX, mouseY, delta);
        this.cancelNewCategoryButton.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.creatingCategory) {
            if (this.newCategoryField.mouseClicked(mouseX, mouseY, button)) return true;
            if (this.saveNewCategoryButton.mouseClicked(mouseX, mouseY, button)) return true;
            if (this.cancelNewCategoryButton.mouseClicked(mouseX, mouseY, button)) return true;
            return true; // Consume clicks outside the dialog
        }

        // Handle dropdown clicks first
        if (categoryDropdown != null && categoryDropdown.isOpen()) {
            if (categoryDropdown.handleDropdownClick(mouseX, mouseY, button)) {
                return true;
            }
            // If click was outside dropdown button, close it
            if (!isMouseOverWidget(categoryDropdown, (int) mouseX, (int) mouseY)) {
                categoryDropdown.close();
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (categoryDropdown != null && categoryDropdown.isOpen()) {
            if (categoryDropdown.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.creatingCategory) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelCreateCategory();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                saveNewCategory();
                return true;
            }
            return this.newCategoryField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.creatingCategory) {
            return this.newCategoryField.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    private void renderFieldLabels(DrawContext context) {
        final int centerX = this.width / 2;
        final int labelX = centerX - FIELD_WIDTH / 2;
        final int startY = 40;
        int currentY = startY;

        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("scaleme.gui.preset.identifier"),
                labelX, currentY - LABEL_OFFSET, 0xFFFFFF
        );
        currentY += FIELD_TO_FIELD_SPACING + LABEL_TO_FIELD_SPACING;

        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("scaleme.gui.preset.display_name.optional"),
                labelX, currentY - LABEL_OFFSET, 0xAAAAAA
        );
        currentY += FIELD_TO_FIELD_SPACING + LABEL_TO_FIELD_SPACING;

        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("scaleme.gui.preset.category"),
                labelX, currentY - LABEL_OFFSET, 0xFFFFFF
        );
        currentY += FIELD_TO_FIELD_SPACING + LABEL_TO_FIELD_SPACING;

        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("scaleme.gui.preset.scale"),
                labelX, currentY - LABEL_OFFSET, 0xFFFFFF
        );
    }

    private void handleTooltips(int mouseX, int mouseY) {
        tooltipText = null;

        // Identifier field tooltip
        if (isMouseOverWidget(identifierField, mouseX, mouseY)) {
            tooltipText = Text.translatable("scaleme.gui.preset.identifier.tooltip");
            tooltipX = mouseX;
            tooltipY = mouseY;
        }
        // Display name field tooltip
        else if (isMouseOverWidget(displayNameField, mouseX, mouseY)) {
            tooltipText = Text.translatable("scaleme.gui.preset.display_name.tooltip");
            tooltipX = mouseX;
            tooltipY = mouseY;
        }
        // Category dropdown tooltip
        else if (isMouseOverWidget(categoryDropdown, mouseX, mouseY)) {
            tooltipText = Text.translatable("scaleme.gui.preset.category.tooltip");
            tooltipX = mouseX;
            tooltipY = mouseY;
        }
        // Scale slider tooltip
        else if (isMouseOverWidget(scaleSlider, mouseX, mouseY)) {
            tooltipText = Text.translatable("scaleme.gui.preset.scale.tooltip");
            tooltipX = mouseX;
            tooltipY = mouseY;
        }
    }

    private boolean isMouseOverWidget(Object widget, int mouseX, int mouseY) {
        if (widget instanceof TextFieldWidget field) {
            return mouseX >= field.getX() && mouseX < field.getX() + field.getWidth() &&
                    mouseY >= field.getY() && mouseY < field.getY() + field.getHeight();
        }
        if (widget instanceof SliderWidget slider) {
            return mouseX >= slider.getX() && mouseX < slider.getX() + slider.getWidth() &&
                    mouseY >= slider.getY() && mouseY < slider.getY() + slider.getHeight();
        }
        if (widget instanceof CategoryDropdownWidget dropdown) {
            return mouseX >= dropdown.getX() && mouseX < dropdown.getX() + dropdown.getWidth() &&
                    mouseY >= dropdown.getY() && mouseY < dropdown.getY() + dropdown.getHeight();
        }
        return false;
    }

    private void savePreset() {
        String identifier = this.identifierField.getText().trim();
        String displayName = this.displayNameField.getText().trim();
        String category = this.categoryDropdown.getSelectedCategory();
        float scale = this.scaleSlider.getScale();
        boolean enabled = this.enabledCheckbox.isChecked();

        if (identifier.isEmpty()) {
            return;
        }

        // Create new preset
        PlayerPreset preset = new PlayerPreset(identifier, displayName.isEmpty() ? null : displayName, scale, category);
        preset.enabled = enabled;

        // If editing, remove the old preset first
        if (isEditing) {
            PlayerPresetManager.removePreset(editingPreset.identifier, editingPreset.isUUID());
        }

        // Add the new/updated preset
        PlayerPresetManager.addPreset(preset);

        // Return to parent screen
        this.client.setScreen(parent);
    }

    private void beginCreateCategory() {
        this.creatingCategory = true;
        setMainWidgetsActive(false);

        final int dialogWidth = 160;
        final int dialogHeight = 90;
        final int dialogX = (this.width - dialogWidth) / 2;
        final int dialogY = (this.height - dialogHeight) / 2;

        this.newCategoryField = new TextFieldWidget(
                this.textRenderer,
                dialogX + 10, dialogY + 30,
                dialogWidth - 20, FIELD_HEIGHT,
                Text.translatable("scaleme.gui.category.new.prompt")
        );
        this.newCategoryField.setPlaceholder(Text.translatable("scaleme.gui.category.new.placeholder"));
        this.newCategoryField.setMaxLength(25);
        this.addDrawableChild(this.newCategoryField);
        this.setInitialFocus(this.newCategoryField);

        int buttonWidth = (dialogWidth - 30) / 2;
        this.saveNewCategoryButton = ButtonWidget.builder(
                Text.translatable("scaleme.gui.preset.save"),
                button -> saveNewCategory()
        ).dimensions(dialogX + 10, dialogY + 60, buttonWidth, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.saveNewCategoryButton);

        this.cancelNewCategoryButton = ButtonWidget.builder(
                ScreenTexts.CANCEL,
                button -> cancelCreateCategory()
        ).dimensions(dialogX + 20 + buttonWidth, dialogY + 60, buttonWidth, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.cancelNewCategoryButton);
    }

    private void saveNewCategory() {
        String newCategory = this.newCategoryField.getText().trim();
        if (!newCategory.isEmpty() && !PlayerPresetManager.getAvailableCategories().contains(newCategory)) {
            PlayerPresetManager.addCategory(newCategory);
            this.categoryDropdown.selectCategory(newCategory);
        }
        cancelCreateCategory();
    }

    private void cancelCreateCategory() {
        this.creatingCategory = false;
        setMainWidgetsActive(true);

        this.remove(this.newCategoryField);
        this.remove(this.saveNewCategoryButton);
        this.remove(this.cancelNewCategoryButton);
        this.newCategoryField = null;
        this.saveNewCategoryButton = null;
        this.cancelNewCategoryButton = null;

        this.setInitialFocus(this.categoryDropdown);
    }

    private void setMainWidgetsActive(boolean active) {
        this.identifierField.setEditable(active);
        this.displayNameField.setEditable(active);
        this.categoryDropdown.active = active;
        this.scaleSlider.active = active;
        this.enabledCheckbox.active = active;
        this.saveButton.active = active && !this.identifierField.getText().trim().isEmpty();
        this.cancelButton.active = active;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (this.creatingCategory) {
            cancelCreateCategory();
            return false;
        }
        return true;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    // Category Dropdown Widget
    private class CategoryDropdownWidget extends ButtonWidget {
        private String selectedCategory;
        private boolean isOpen = false;
        private final List<String> categories;
        private final Text ADD_NEW_TEXT = Text.translatable("scaleme.gui.category.new").formatted(Formatting.GREEN, Formatting.ITALIC);

        // For scrolling
        private int scrollOffset = 0;
        private static final int MAX_VISIBLE_ITEMS = 6;
        private static final int ITEM_HEIGHT = 18;

        public CategoryDropdownWidget(int x, int y, int width, int height, String initialCategory) {
            super(x, y, width, height, Text.literal(initialCategory), null, DEFAULT_NARRATION_SUPPLIER);
            this.selectedCategory = initialCategory;
            this.categories = new ArrayList<>();
            refreshCategories(); // Initial population
            selectCategory(initialCategory);
        }

        private void updateMessage() {
            setMessage(Text.literal(selectedCategory + " ▼"));
        }

        public boolean isOpen() {
            return isOpen;
        }

        public void close() {
            this.isOpen = false;
        }

        public String getSelectedCategory() {
            return selectedCategory;
        }

        public void refreshCategories() {
            String current = this.selectedCategory;
            this.categories.clear();
            this.categories.addAll(PlayerPresetManager.getAvailableCategories());
            if (!this.categories.contains("default")) {
                this.categories.add(0, "default");
            }
            if (!this.categories.contains(current)) {
                this.selectedCategory = "default";
            }
        }

        public void selectCategory(String category) {
            refreshCategories();
            this.selectedCategory = category;
            updateMessage();
        }

        @Override
        public void onPress() {
            this.isOpen = !this.isOpen;
            if (this.isOpen) {
                refreshCategories();
            }
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
        }

        public int[] getDropdownBounds() {
            if (!isOpen) {
                return new int[]{0, 0, 0, 0};
            }

            int totalItems = categories.size() + 1; // +1 for "Add New..."
            int visibleItems = Math.min(totalItems, MAX_VISIBLE_ITEMS);
            int dropdownHeight = visibleItems * ITEM_HEIGHT;
            int optionY = getY() + getHeight() + 2;

            if (optionY + dropdownHeight > AddEditPresetScreen.this.height) {
                optionY = getY() - dropdownHeight - 2;
            }

            return new int[]{getX(), optionY, getWidth(), dropdownHeight};
        }

        public void renderDropdown(DrawContext context, int mouseX, int mouseY) {
            if (!isOpen) return;

            int[] bounds = getDropdownBounds();
            int x = bounds[0];
            int y = bounds[1];
            int width = bounds[2];
            int height = bounds[3];

            context.fill(x, y, x + width, y + height, 0xFF1E1E1E);
            context.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF2D2D30);
            context.drawBorder(x, y, width, height, 0xFF666666);

            int totalItems = categories.size() + 1;
            int visibleItems = Math.min(totalItems, MAX_VISIBLE_ITEMS);

            for (int i = 0; i < visibleItems; i++) {
                int index = i + scrollOffset;
                int itemY = y + i * ITEM_HEIGHT;

                boolean isHovered = mouseX >= x && mouseX < x + width &&
                        mouseY >= itemY && mouseY < itemY + ITEM_HEIGHT;

                if (isHovered) {
                    context.fill(x + 1, itemY, x + width - 1, itemY + ITEM_HEIGHT - 1, 0xFF404040);
                }

                if (index < categories.size()) {
                    String category = categories.get(index);
                    context.drawTextWithShadow(textRenderer, category, x + 5, itemY + 5, 0xFFFFFF);
                } else { // "Add New..." option
                    context.drawTextWithShadow(textRenderer, ADD_NEW_TEXT, x + 5, itemY + 5, 0xFFFFFF);
                }
            }

            if (totalItems > MAX_VISIBLE_ITEMS) {
                renderScrollbar(context, bounds);
            }
        }

        private void renderScrollbar(DrawContext context, int[] bounds) {
            int x = bounds[0];
            int y = bounds[1];
            int width = bounds[2];
            int height = bounds[3];

            int totalItems = categories.size() + 1;
            int scrollbarX = x + width - 6;
            int scrollbarHeight = height;

            // Scrollbar track
            context.fill(scrollbarX, y, scrollbarX + 5, y + scrollbarHeight, 0xFF101010);

            // Scrollbar handle
            int handleHeight = (int) ((float) scrollbarHeight * ((float) MAX_VISIBLE_ITEMS / totalItems));
            handleHeight = Math.max(handleHeight, 5); // Minimum handle height
            int handleY = y + (int) ((float) (scrollbarHeight - handleHeight) * ((float) this.scrollOffset / (totalItems - MAX_VISIBLE_ITEMS)));

            context.fill(scrollbarX + 1, handleY + 1, scrollbarX + 4, handleY + handleHeight - 1, 0xFF888888);
            context.drawBorder(scrollbarX, handleY, 5, handleHeight, 0xFFCCCCCC);
        }

        public boolean handleDropdownClick(double mouseX, double mouseY, int button) {
            if (!isOpen) return false;

            int[] bounds = getDropdownBounds();
            int x = bounds[0];
            int y = bounds[1];
            int width = bounds[2];
            int height = bounds[3];
            int totalItems = categories.size() + 1;

            if (mouseY >= y && mouseY < y + height && mouseX >= x && mouseX < x + width) {
                int clickedIndex = scrollOffset + (int) ((mouseY - y) / ITEM_HEIGHT);

                if (clickedIndex < categories.size()) {
                    selectCategory(categories.get(clickedIndex));
                } else if (clickedIndex == categories.size()) {
                    // "Add New..." clicked
                    beginCreateCategory();
                }

                this.isOpen = false;
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (isOpen) {
                int totalItems = categories.size() + 1;
                if (totalItems > MAX_VISIBLE_ITEMS) {
                    // verticalAmount is -1 for scroll down, 1 for scroll up in vanilla
                    if (verticalAmount < 0) { // Scroll down
                        this.scrollOffset = Math.min(this.scrollOffset + 1, totalItems - MAX_VISIBLE_ITEMS);
                    } else if (verticalAmount > 0) { // Scroll up
                        this.scrollOffset = Math.max(this.scrollOffset - 1, 0);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    // Scale Slider Widget
    private static class ScaleSliderWidget extends SliderWidget {
        private static final float MIN_SCALE = 0.1f;
        private static final float MAX_SCALE = 3.0f;

        public ScaleSliderWidget(int x, int y, int width, int height, float initialScale) {
            super(x, y, width, height,
                    Text.translatable("scaleme.gui.preset.scale_value", String.format("%.1f", initialScale)),
                    (initialScale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE)
            );
        }

        @Override
        protected void updateMessage() {
            float scale = getScale();
            this.setMessage(Text.translatable("scaleme.gui.preset.scale_value", String.format("%.1f", scale)));
        }

        @Override
        protected void applyValue() {
            // Value is applied when getScale() is called
        }

        public float getScale() {
            return MIN_SCALE + (float) this.value * (MAX_SCALE - MIN_SCALE);
        }
    }
}