package com.github.scaleme.client.gui;

import com.github.scaleme.client.data.PlayerPreset;
import com.github.scaleme.client.util.PlayerPresetManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerPresetScreen extends Screen {
    private final Screen parent;
    private PlayerPresetListWidget presetList;
    private PlayerPreset selectedPreset;
    private TextFieldWidget searchField;

    // Sort buttons
    private ButtonWidget sortByNameButton;
    private ButtonWidget sortByCategoryButton;
    private ButtonWidget sortByScaleButton;

    // Category filter dropdown
    private CategoryFilterDropdown categoryFilterDropdown;

    // Action buttons
    private ButtonWidget addButton;
    private ButtonWidget editButton;
    private ButtonWidget deleteButton;
    private ButtonWidget doneButton;

    // Current state
    private PlayerPresetManager.SortType currentSort = PlayerPresetManager.SortType.NAME;
    private boolean sortAscending = true;
    private Set<String> selectedCategories = new HashSet<>();
    private String currentSearchTerm = "";

    // Tooltip tracking
    private Text currentTooltip = null;
    private int tooltipX, tooltipY;

    // Layout constants
    private static final int MARGIN = 15;
    private static final int SEARCH_WIDTH = 300;
    private static final int SEARCH_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SORT_BUTTON_WIDTH = 55; // Reduced from 60
    private static final int DROPDOWN_WIDTH = 130;
    private static final int ACTION_BUTTON_WIDTH = 90;
    private static final int ELEMENT_SPACING = 8;

    public PlayerPresetScreen(Screen parent) {
        super(Text.translatable("scaleme.gui.presets.title"));
        this.parent = parent;
        this.selectedCategories.clear();
    }

    @Override
    protected void init() {
        super.init();

        final int centerX = this.width / 2;
        int currentY = 35;

        // Search field with icon
        this.searchField = new TextFieldWidget(
                this.textRenderer,
                centerX - SEARCH_WIDTH / 2, currentY,
                SEARCH_WIDTH, SEARCH_HEIGHT,
                Text.translatable("scaleme.gui.presets.search")
        );
        this.searchField.setPlaceholder(
                Text.translatable("scaleme.gui.presets.search.placeholder.detailed").formatted(Formatting.GRAY)
        );
        this.searchField.setChangedListener(this::onSearchChanged);
        this.addDrawableChild(this.searchField);
        currentY += SEARCH_HEIGHT + ELEMENT_SPACING;

        // Sort buttons and filter on same line
        int categoryButtonWidth = SORT_BUTTON_WIDTH + 20; // For the longer "Category" text
        int sortFilterGap = 50; // Increased gap to push filter to the right
        int controlRowTotalWidth = (SORT_BUTTON_WIDTH * 2 + categoryButtonWidth) + (ELEMENT_SPACING * 2) + sortFilterGap + DROPDOWN_WIDTH;
        int currentX = centerX - controlRowTotalWidth / 2;

        this.sortByNameButton = ButtonWidget.builder(
                Text.literal("Name " + getSortArrow(PlayerPresetManager.SortType.NAME)),
                button -> toggleSort(PlayerPresetManager.SortType.NAME)
        ).dimensions(currentX, currentY, SORT_BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.sortByNameButton);
        currentX += SORT_BUTTON_WIDTH + ELEMENT_SPACING;

        this.sortByCategoryButton = ButtonWidget.builder(
                Text.literal("Category " + getSortArrow(PlayerPresetManager.SortType.CATEGORY)),
                button -> toggleSort(PlayerPresetManager.SortType.CATEGORY)
        ).dimensions(currentX, currentY, categoryButtonWidth, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.sortByCategoryButton);
        currentX += categoryButtonWidth + ELEMENT_SPACING;

        this.sortByScaleButton = ButtonWidget.builder(
                Text.literal("Scale " + getSortArrow(PlayerPresetManager.SortType.SCALE)),
                button -> toggleSort(PlayerPresetManager.SortType.SCALE)
        ).dimensions(currentX, currentY, SORT_BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.sortByScaleButton);
        currentX += SORT_BUTTON_WIDTH + sortFilterGap; // Apply larger gap

        // Category filter dropdown
        this.categoryFilterDropdown = new CategoryFilterDropdown(
                currentX, currentY, DROPDOWN_WIDTH, BUTTON_HEIGHT,
                selectedCategories
        );
        this.addDrawableChild(this.categoryFilterDropdown);
        currentY += BUTTON_HEIGHT + ELEMENT_SPACING;

        // Preset list
        int listTop = currentY;
        int listBottom = this.height - 60;
        this.presetList = new PlayerPresetListWidget(
                this.client,
                this.width - (MARGIN * 2),
                listBottom - listTop,
                listTop,
                26
        );
        this.presetList.setX(MARGIN);
        this.addSelectableChild(this.presetList);

        // Action buttons
        int bottomButtonY = this.height - 30;
        int totalButtonWidth = (ACTION_BUTTON_WIDTH * 4) + (ELEMENT_SPACING * 3);
        int buttonStartX = centerX - (totalButtonWidth / 2);

        this.addButton = ButtonWidget.builder(
                Text.translatable("scaleme.gui.presets.add"),
                button -> this.client.setScreen(new AddEditPresetScreen(this, null))
        ).dimensions(buttonStartX, bottomButtonY, ACTION_BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.addButton);

        this.editButton = ButtonWidget.builder(
                Text.translatable("scaleme.gui.presets.edit_selected"),
                button -> editSelectedPreset()
        ).dimensions(buttonStartX + ACTION_BUTTON_WIDTH + ELEMENT_SPACING, bottomButtonY, ACTION_BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.editButton);

        this.deleteButton = ButtonWidget.builder(
                Text.translatable("scaleme.gui.presets.delete"),
                button -> deleteSelectedPreset()
        ).dimensions(buttonStartX + (ACTION_BUTTON_WIDTH + ELEMENT_SPACING) * 2, bottomButtonY, ACTION_BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.deleteButton);

        this.doneButton = ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> this.client.setScreen(parent)
        ).dimensions(buttonStartX + (ACTION_BUTTON_WIDTH + ELEMENT_SPACING) * 3, bottomButtonY, ACTION_BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.doneButton);

        refreshPresetList();
        updateButtonStates();
        this.setInitialFocus(this.searchField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("scaleme.gui.presets.title_full"),
                this.width / 2,
                15,
                0xFFFFFF
        );

        // Search icon
        context.drawTextWithShadow(
                this.textRenderer,
                "🔍",
                this.searchField.getX() + this.searchField.getWidth() + 5,
                this.searchField.getY() + 6,
                0xFFFFFF
        );

        // Sort by and Filter labels
        Text sortByText = Text.literal("Sort by:");
        context.drawTextWithShadow(
                this.textRenderer,
                sortByText,
                this.sortByNameButton.getX() - this.textRenderer.getWidth(sortByText) - 5,
                this.sortByNameButton.getY() + 6,
                0xAAAAAA
        );

        Text filterByText = Text.literal("Filter:");
        context.drawTextWithShadow(
                this.textRenderer,
                filterByText,
                this.categoryFilterDropdown.getX() - this.textRenderer.getWidth(filterByText) - 5,
                this.categoryFilterDropdown.getY() + 6,
                0xAAAAAA
        );

        // Show active category filters
        if (!selectedCategories.isEmpty()) {
            String filterText = "(" + String.join(", ", selectedCategories) + ")";
            context.drawTextWithShadow(
                    this.textRenderer,
                    filterText,
                    this.categoryFilterDropdown.getX() + this.categoryFilterDropdown.getWidth() + 10,
                    this.categoryFilterDropdown.getY() + 6,
                    0x55AAFF
            );
        }

        updateButtonStates();

        // Render preset list
        this.presetList.render(context, mouseX, mouseY, delta);

        // Status text
        List<PlayerPreset> allPresets = PlayerPresetManager.getAllPresets();
        List<PlayerPreset> filteredPresets = getFilteredPresets();

        String countText = Text.translatable("scaleme.gui.presets.showing_count",
                filteredPresets.size(), allPresets.size()).getString();
        context.drawTextWithShadow(
                this.textRenderer,
                countText,
                MARGIN, this.height - 50,
                0xAAAAAA
        );

        // Selection help text
        if (selectedPreset == null) {
            String helpText = Text.translatable("scaleme.gui.presets.select_to_edit").getString();
            int helpWidth = this.textRenderer.getWidth(helpText);
            context.drawTextWithShadow(
                    this.textRenderer,
                    helpText,
                    this.width - MARGIN - helpWidth, this.height - 50,
                    0x888888
            );
        }

        // Render dropdown LAST and at a higher Z-index to ensure it's on top
        if (categoryFilterDropdown != null && categoryFilterDropdown.isOpen()) {
            context.getMatrices().push();
            context.getMatrices().translate(0.0, 0.0, 400.0);
            categoryFilterDropdown.renderDropdown(context, mouseX, mouseY);
            context.getMatrices().pop();
        }

        // Render tooltip if available (only if dropdown is not open)
        if (currentTooltip != null && (categoryFilterDropdown == null || !categoryFilterDropdown.isOpen())) {
            context.drawTooltip(this.textRenderer, currentTooltip, tooltipX, tooltipY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle dropdown clicks first
        if (categoryFilterDropdown != null && categoryFilterDropdown.isOpen()) {
            if (categoryFilterDropdown.handleDropdownClick(mouseX, mouseY, button)) {
                return true; // Dropdown handled the click
            }
            // If click was outside dropdown, close it
            if (!isPointInDropdownButton(mouseX, mouseY)) {
                categoryFilterDropdown.closeDropdown();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isPointInDropdownButton(double mouseX, double mouseY) {
        return categoryFilterDropdown != null &&
                mouseX >= categoryFilterDropdown.getX() &&
                mouseX < categoryFilterDropdown.getX() + categoryFilterDropdown.getWidth() &&
                mouseY >= categoryFilterDropdown.getY() &&
                mouseY < categoryFilterDropdown.getY() + categoryFilterDropdown.getHeight();
    }

    private String getSortArrow(PlayerPresetManager.SortType sortType) {
        if (currentSort == sortType) {
            return sortAscending ? "▲" : "▼";
        }
        return "";
    }

    private void toggleSort(PlayerPresetManager.SortType sortType) {
        if (currentSort == sortType) {
            sortAscending = !sortAscending;
        } else {
            currentSort = sortType;
            sortAscending = true;
        }

        updateSortButtonTexts();
        refreshPresetList();
    }

    private void updateSortButtonTexts() {
        this.sortByNameButton.setMessage(Text.literal("Name " + getSortArrow(PlayerPresetManager.SortType.NAME)));
        this.sortByCategoryButton.setMessage(Text.literal("Category " + getSortArrow(PlayerPresetManager.SortType.CATEGORY)));
        this.sortByScaleButton.setMessage(Text.literal("Scale " + getSortArrow(PlayerPresetManager.SortType.SCALE)));
    }

    private List<PlayerPreset> getFilteredPresets() {
        List<PlayerPreset> presets = PlayerPresetManager.getAllPresets();

        // Apply search filter
        if (!currentSearchTerm.trim().isEmpty()) {
            String search = currentSearchTerm.toLowerCase().trim();
            presets = presets.stream()
                    .filter(preset ->
                            preset.getEffectiveDisplayName().toLowerCase().contains(search) ||
                                    preset.identifier.toLowerCase().contains(search) ||
                                    preset.category.toLowerCase().contains(search) ||
                                    String.format("%.1f", preset.scale).contains(search)
                    )
                    .collect(Collectors.toList());
        }

        // Apply category filter
        if (!selectedCategories.isEmpty()) {
            presets = presets.stream()
                    .filter(preset -> selectedCategories.contains(preset.category))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        switch (currentSort) {
            case NAME:
                presets.sort(Comparator.comparing(PlayerPreset::getEffectiveDisplayName, String.CASE_INSENSITIVE_ORDER));
                break;
            case CATEGORY:
                presets.sort(Comparator.comparing((PlayerPreset p) -> p.category, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PlayerPreset::getEffectiveDisplayName, String.CASE_INSENSITIVE_ORDER));
                break;
            case SCALE:
                presets.sort(Comparator.comparing((PlayerPreset p) -> p.scale));
                break;
            case ENABLED:
                presets.sort(Comparator.comparing((PlayerPreset p) -> p.enabled).reversed()
                        .thenComparing(PlayerPreset::getEffectiveDisplayName, String.CASE_INSENSITIVE_ORDER));
                break;
        }

        if (!sortAscending) {
            Collections.reverse(presets);
        }

        return presets;
    }

    public void setTooltip(Text tooltip, int x, int y) {
        this.currentTooltip = tooltip;
        this.tooltipX = x;
        this.tooltipY = y;
    }

    public void clearTooltip() {
        this.currentTooltip = null;
    }

    private void onSearchChanged(String searchTerm) {
        currentSearchTerm = searchTerm;
        refreshPresetList();
    }

    private void refreshPresetList() {
        List<PlayerPreset> presets = getFilteredPresets();
        this.presetList.setPresets(presets);
    }

    private void updateButtonStates() {
        this.sortByNameButton.active = currentSort != PlayerPresetManager.SortType.NAME;
        this.sortByCategoryButton.active = currentSort != PlayerPresetManager.SortType.CATEGORY;
        this.sortByScaleButton.active = currentSort != PlayerPresetManager.SortType.SCALE;

        boolean hasSelection = selectedPreset != null;
        this.editButton.active = hasSelection;
        this.deleteButton.active = hasSelection;
    }

    private void editSelectedPreset() {
        if (selectedPreset != null) {
            this.client.setScreen(new AddEditPresetScreen(this, selectedPreset));
        }
    }

    private void deleteSelectedPreset() {
        if (selectedPreset != null) {
            PlayerPresetManager.removePreset(selectedPreset.identifier, selectedPreset.isUUID());
            refreshPresetList();
            selectedPreset = null;
        }
    }

    public void setSelectedPreset(PlayerPreset preset) {
        this.selectedPreset = preset;
    }

    public void updateCategoryFilter(Set<String> categories) {
        this.selectedCategories = new HashSet<>(categories);
        refreshPresetList();
    }

    public boolean isDropdownOpen() {
        return categoryFilterDropdown != null && categoryFilterDropdown.isOpen();
    }

    public int[] getDropdownBounds() {
        if (categoryFilterDropdown == null || !categoryFilterDropdown.isOpen()) {
            return new int[]{0, 0, 0, 0};
        }
        return categoryFilterDropdown.getDropdownBounds();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    // UI-aligned category filter dropdown that stays open
    private class CategoryFilterDropdown extends ButtonWidget {
        private final Set<String> selectedCategories;
        private boolean isOpen = false;
        private final List<String> availableCategories;

        public CategoryFilterDropdown(int x, int y, int width, int height, Set<String> selectedCategories) {
            super(x, y, width, height, Text.translatable("scaleme.gui.presets.categories"), null, DEFAULT_NARRATION_SUPPLIER);
            this.selectedCategories = selectedCategories;
            this.availableCategories = PlayerPresetManager.getAllPresets().stream()
                    .map(preset -> preset.category)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            updateMessage();
        }

        private void updateMessage() {
            if (selectedCategories.isEmpty()) {
                setMessage(Text.literal("Categories ▼"));
            } else if (selectedCategories.size() == 1) {
                setMessage(Text.literal(selectedCategories.iterator().next() + " ▼"));
            } else {
                setMessage(Text.literal(selectedCategories.size() + " selected ▼"));
            }
        }

        public boolean isOpen() {
            return isOpen;
        }

        public void closeDropdown() {
            isOpen = false;
        }

        public int[] getDropdownBounds() {
            if (!isOpen || availableCategories.isEmpty()) {
                return new int[]{0, 0, 0, 0};
            }

            int maxOptions = Math.min(availableCategories.size() + 1, 6);
            int itemHeight = 18;
            int dropdownHeight = maxOptions * itemHeight;
            int optionY = getY() + getHeight() + 2;

            // Check if dropdown needs to render above
            if (optionY + dropdownHeight > PlayerPresetScreen.this.height - 60) {
                optionY = getY() - dropdownHeight - 2;
            }

            return new int[]{getX(), optionY, getWidth(), dropdownHeight};
        }

        @Override
        public void onPress() {
            isOpen = !isOpen;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // Render the button using vanilla style
            super.renderWidget(context, mouseX, mouseY, delta);
            // Dropdown is rendered separately in main render method
        }

        public void renderDropdown(DrawContext context, int mouseX, int mouseY) {
            if (!isOpen || availableCategories.isEmpty()) return;

            int[] bounds = getDropdownBounds();
            int x = bounds[0];
            int y = bounds[1];
            int width = bounds[2];
            int height = bounds[3];

            // Draw dropdown background matching the UI theme (dark gray instead of white)
            context.fill(x, y, x + width, y + height, 0xFF1E1E1E); // Dark background
            context.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF2D2D30); // Slightly lighter inner
            context.drawBorder(x, y, width, height, 0xFF666666); // Gray border

            int itemHeight = 18;

            // "All Categories" option
            boolean allHovered = mouseX >= x && mouseX < x + width &&
                    mouseY >= y && mouseY < y + itemHeight;

            if (allHovered) {
                context.fill(x + 1, y + 1, x + width - 1, y + itemHeight - 1, 0xFF404040);
            }

            String allText = selectedCategories.isEmpty() ? "✓ All Categories" : "All Categories";
            int allTextColor = selectedCategories.isEmpty() ? 0x55FF55 : 0xFFFFFF;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, allText,
                    x + 4, y + 5, allTextColor);

            // Category options
            for (int i = 0; i < availableCategories.size() && i < 5; i++) {
                String category = availableCategories.get(i);
                int categoryY = y + (i + 1) * itemHeight;
                boolean categoryHovered = mouseX >= x && mouseX < x + width &&
                        mouseY >= categoryY && mouseY < categoryY + itemHeight;

                if (categoryHovered) {
                    context.fill(x + 1, categoryY + 1, x + width - 1, categoryY + itemHeight - 1, 0xFF404040);
                }

                boolean isSelected = selectedCategories.contains(category);
                String categoryText = isSelected ? "✓ " + category : category;
                int textColor = isSelected ? 0x55FF55 : 0xFFFFFF;

                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, categoryText,
                        x + 4, categoryY + 5, textColor);
            }
        }

        public boolean handleDropdownClick(double mouseX, double mouseY, int button) {
            if (!isOpen || availableCategories.isEmpty()) return false;

            int[] bounds = getDropdownBounds();
            int x = bounds[0];
            int y = bounds[1];
            int width = bounds[2];
            int height = bounds[3];

            if (mouseY >= y && mouseY < y + height &&
                    mouseX >= x && mouseX < x + width) {
                int optionIndex = (int) ((mouseY - y) / 18);

                if (optionIndex == 0) {
                    // "All Categories" clicked
                    selectedCategories.clear();
                } else if (optionIndex - 1 < availableCategories.size()) {
                    // Category clicked
                    String category = availableCategories.get(optionIndex - 1);
                    if (selectedCategories.contains(category)) {
                        selectedCategories.remove(category);
                    } else {
                        selectedCategories.add(category);
                    }
                }

                updateMessage();
                PlayerPresetScreen.this.updateCategoryFilter(selectedCategories);
                // DON'T close dropdown - let it stay open for multiple selections
                return true;
            }

            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (isOpen) {
                isOpen = false;
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
    }
}