package com.github.scaleme.client.gui;

import com.github.scaleme.client.data.PlayerPreset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class PlayerPresetListWidget extends AlwaysSelectedEntryListWidget<PlayerPresetListWidget.PresetEntry> {
    private final PlayerPresetScreen parent;

    public PlayerPresetListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        if (client.currentScreen instanceof PlayerPresetScreen) {
            this.parent = (PlayerPresetScreen) client.currentScreen;
        } else {
            this.parent = null;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Block all mouse interactions if dropdown is open
        if (parent != null && parent.isDropdownOpen()) {
            int[] bounds = parent.getDropdownBounds();
            if (bounds[2] > 0 && bounds[3] > 0) {
                // Check if mouse is within dropdown bounds
                if (mouseX >= bounds[0] && mouseX < bounds[0] + bounds[2] &&
                        mouseY >= bounds[1] && mouseY < bounds[1] + bounds[3]) {
                    return false; // Let dropdown handle the click
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (parent != null && parent.isDropdownOpen()) {
            return false;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (parent != null && parent.isDropdownOpen()) {
            return false;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (parent != null && parent.isDropdownOpen()) {
            return; // Don't process mouse movement if dropdown is open
        }
        super.mouseMoved(mouseX, mouseY);
    }

    public void setPresets(List<PlayerPreset> presets) {
        this.clearEntries();
        for (PlayerPreset preset : presets) {
            this.addEntry(new PresetEntry(preset));
        }
    }

    @Override
    public void setSelected(PresetEntry entry) {
        super.setSelected(entry);
        if (parent != null) {
            parent.setSelectedPreset(entry != null ? entry.preset : null);
        }
    }

    @Override
    public int getRowWidth() {
        return this.getWidth() - 20;
    }

    @Override
    protected int getScrollbarX() {
        return this.getX() + this.getWidth() - 8;
    }

    public class PresetEntry extends AlwaysSelectedEntryListWidget.Entry<PresetEntry> {
        private final PlayerPreset preset;

        public PresetEntry(PlayerPreset preset) {
            this.preset = preset;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            MinecraftClient client = MinecraftClient.getInstance();

            // Block hover effects if dropdown is open
            if (parent != null && parent.isDropdownOpen()) {
                hovered = false;
            }

            // Background highlighting
            boolean isSelected = this.equals(PlayerPresetListWidget.this.getSelectedOrNull());
            if (isSelected) {
                context.fill(x, y, x + entryWidth, y + entryHeight, 0xFF444444);
                context.drawBorder(x, y, entryWidth, entryHeight, 0xFF666666);
            } else if (hovered) {
                context.fill(x, y, x + entryWidth, y + entryHeight, 0xFF222222);
            }

            // Thin dividing line at bottom
            if (index < PlayerPresetListWidget.this.children().size() - 1) {
                context.fill(x + 10, y + entryHeight - 1, x + entryWidth - 10, y + entryHeight, 0xFF333333);
            }

            // Calculate available height for text (excluding divider)
            int availableHeight = entryHeight - 1;

            // Status indicator
            int statusX = x + 6;
            int statusY = y + (availableHeight / 2) - 4;

            if (preset.enabled) {
                context.drawTextWithShadow(client.textRenderer, "✔", statusX, statusY, 0xFF55FF55);
            } else {
                context.drawTextWithShadow(client.textRenderer, "❌", statusX, statusY, 0xFF888888);
            }

            // Display name layout
            int nameX = x + 22;
            int nameColor = preset.enabled ? 0xFFFFFF : 0x888888;

            String primaryText;
            String secondaryText = null;
            boolean hasDisplayName = preset.displayName != null && !preset.displayName.trim().isEmpty();

            if (hasDisplayName) {
                primaryText = preset.displayName.trim();
                String identifierType = preset.isUUID() ? "UUID" : "Username";
                secondaryText = identifierType + ": " + preset.identifier;
            } else {
                primaryText = preset.identifier;
            }

            // Calculate vertical centering
            int textHeight = 9;
            int totalTextHeight;
            int startY;

            if (secondaryText != null) {
                totalTextHeight = textHeight + 2 + textHeight;
                startY = y + (availableHeight - totalTextHeight) / 2;
            } else {
                startY = y + (availableHeight - textHeight) / 2;
            }

            // Draw primary text
            context.drawTextWithShadow(client.textRenderer, primaryText, nameX, startY, nameColor);

            // Draw secondary text if present
            if (secondaryText != null) {
                context.drawText(client.textRenderer, secondaryText, nameX, startY + textHeight + 2, 0xFF666666, false);
            }

            // Scale
            String scaleText = String.format("Scale: %.1fx", preset.scale);
            int scaleColor = getScaleColorValue(preset.scale);
            int scaleWidth = client.textRenderer.getWidth(scaleText);
            int scaleX = x + entryWidth - scaleWidth - 120;
            int scaleY = y + (availableHeight / 2) - 4;
            context.drawTextWithShadow(client.textRenderer, scaleText, scaleX, scaleY, scaleColor);

            // Category tag
            String categoryTag = "[" + preset.category + "]";
            int categoryWidth = client.textRenderer.getWidth(categoryTag);
            int categoryX = x + entryWidth - categoryWidth - 10;
            int categoryY = y + (availableHeight / 2) - 4;
            context.drawTextWithShadow(client.textRenderer, categoryTag, categoryX, categoryY, 0xFF999999);

            // REMOVED: Tooltip handling - no more tooltips for list entries
        }

        private int getScaleColorValue(float scale) {
            if (scale <= 0.2f) return 0xFFFF8844;
            if (scale < 0.5f) return 0xFFFFAA44;
            if (scale < 0.8f) return 0xFFFFCC44;
            if (scale >= 0.9f && scale <= 1.1f) return 0xFFFFFFFF;
            if (scale <= 1.5f) return 0xFF88FF88;
            if (scale <= 2.0f) return 0xFF44AAFF;
            if (scale <= 2.5f) return 0xFFAA88FF;
            return 0xFFFF8844;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Don't handle clicks if dropdown is open
            if (parent != null && parent.isDropdownOpen()) {
                return false;
            }
            PlayerPresetListWidget.this.setSelected(this);
            return true;
        }

        @Override
        public Text getNarration() {
            String statusText = preset.enabled ?
                    Text.translatable("scaleme.gui.presets.enabled").getString() :
                    Text.translatable("scaleme.gui.presets.disabled").getString();

            return Text.translatable(
                    "scaleme.gui.presets.entry_narration_simple",
                    preset.getEffectiveDisplayName(),
                    String.format("%.1f", preset.scale),
                    preset.category,
                    statusText
            );
        }
    }
}