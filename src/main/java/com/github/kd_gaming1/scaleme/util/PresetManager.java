package com.github.kd_gaming1.scaleme.util;

import com.github.kd_gaming1.scaleme.ScaleMe;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonWriter;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Export / import manager for config presets.
 * <p>
 * Presets are exchanged as compact JSON text via the clipboard — no files.
 * Category-scoped exports let users share just item animations, just scales, etc.
 */
public final class PresetManager {

    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new Gson();

    /** Category → ordered map of field-name → default-value. */
    private static final Map<String, Map<String, Object>> DEFAULTS = new LinkedHashMap<>();

    static {
        Map<String, Object> hand = new LinkedHashMap<>();
        hand.put("enableHandItemTransform", false);
        hand.put("enableArmPositionOverride", false);
        hand.put("armBaseX", 0.56f);
        hand.put("armBaseY", -0.52f);
        hand.put("armBaseZ", -0.72f);
        hand.put("armHeightScale", -0.6f);
        hand.put("enableItemTransformOverride", false);
        hand.put("itemScale", 1f);
        hand.put("itemTranslationX", 0f);
        hand.put("itemTranslationY", 0f);
        hand.put("itemTranslationZ", 0f);
        hand.put("itemRotationX", 0f);
        hand.put("itemRotationY", 0f);
        hand.put("itemRotationZ", 0f);
        hand.put("enableSeparateHandTransforms", false);
        hand.put("armBaseXOffhand", 0.56f);
        hand.put("armBaseYOffhand", -0.52f);
        hand.put("armBaseZOffhand", -0.72f);
        hand.put("armHeightScaleOffhand", -0.6f);
        hand.put("itemScaleOffhand", 1f);
        hand.put("itemTranslationXOffhand", 0f);
        hand.put("itemTranslationYOffhand", 0f);
        hand.put("itemTranslationZOffhand", 0f);
        hand.put("itemRotationXOffhand", 0f);
        hand.put("itemRotationYOffhand", 0f);
        hand.put("itemRotationZOffhand", 0f);
        DEFAULTS.put(ScaleMeConfig.HAND, hand);

        Map<String, Object> anim = new LinkedHashMap<>();
        anim.put("enableSwordBlock", false);
        anim.put("enableAnimOverrides", false);
        anim.put("disableSwingBobbing", false);
        anim.put("ignoreSwingSpeedEffects", false);
        anim.put("swingAnimationSpeed", 1f);
        anim.put("disableSwingAnimation", false);
        anim.put("enableSwingOverride", false);
        anim.put("swingArmXScale", -0.4f);
        anim.put("swingArmYScale", 0.2f);
        anim.put("swingArmZScale", -0.2f);
        anim.put("swingArmXMultiplyBySide", true);
        anim.put("swingPreRotationY", 45f);
        anim.put("swingArcYAmount", -20f);
        anim.put("swingArcZAmount", -20f);
        anim.put("swingArcXAmount", -80f);
        anim.put("swingCounterRotation", true);
        DEFAULTS.put(ScaleMeConfig.ANIM, anim);

        Map<String, Object> scale = new LinkedHashMap<>();
        scale.put("scaleNameTags", false);
        scale.put("playerScale", 1f);
        scale.put("otherPlayersScale", 1f);
        scale.put("villagerNpcScale", 1f);
        scale.put("hypixelNpcScale", 1f);
        DEFAULTS.put(ScaleMeConfig.SCALE, scale);

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("enableCrosshairInThirdPerson", false);
        view.put("enableCrosshairInThirdPersonFront", false);
        view.put("disableSelfieCam", false);
        view.put("showOwnNametagInThirdPerson", false);
        view.put("hidePlayers", false);
        view.put("hidePlayersOnlyOnSkyblock", false);
        DEFAULTS.put(ScaleMeConfig.VIEW, view);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("enableGroundItemScale", false);
        item.put("groundItemScale", 1f);
        DEFAULTS.put(ScaleMeConfig.ITEM, item);
    }

    private PresetManager() {}

    /**
     * Exports the current config as a clickable chat component.
     *
     * @param category null for full export, or one of HAND/ANIM/SCALE/VIEW/ITEM
     * @return chat message containing the JSON and a copy button
     */
    public static Component exportToChat(String category) {
        String json = buildExportJson(category);
        String preview = buildPreview(category);

        MutableComponent msg = Component.literal("§a[Scale Me] §fPreset exported: " + preview + " ");

        MutableComponent copyBtn = Component.literal("§2[Copy JSON]")
                .withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent.CopyToClipboard(json))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to copy preset JSON"))));

        MutableComponent instruct = Component.literal("\n§7Use §f/scaleme import <json> §7to apply this preset.");

        return msg.append(copyBtn).append(instruct);
    }

    /**
     * Imports a preset from a JSON string.
     *
     * @param json raw JSON string
     * @return human-readable result message
     */
    public static String importFromJson(String json) {
        JsonObject root;
        try {
            root = GSON.fromJson(json, JsonObject.class);
        } catch (JsonParseException e) {
            ScaleMe.LOGGER.warn("Preset import failed: invalid JSON — {}", e.getMessage());
            return "§cInvalid JSON. Make sure you copied the whole string.";
        }

        if (root == null || !root.has("v") || !root.has("d")) {
            return "§cInvalid preset format. Expected 'v' and 'd' fields.";
        }

        int version = root.get("v").getAsInt();
        if (version != SCHEMA_VERSION) {
            return "§cUnsupported preset version (v" + version + "). This mod expects v" + SCHEMA_VERSION + ".";
        }

        JsonObject data = root.getAsJsonObject("d");
        if (data == null || data.size() == 0) {
            return "§cPreset contains no data.";
        }

        int applied = 0;
        int skipped = 0;

        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            JsonElement value = entry.getValue();

            Field field;
            try {
                field = ScaleMeConfig.class.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                ScaleMe.LOGGER.debug("Preset skipped unknown field: {}", fieldName);
                skipped++;
                continue;
            }

            try {
                applyField(field, value);
                applied++;
            } catch (IllegalAccessException | IllegalArgumentException e) {
                ScaleMe.LOGGER.warn("Preset failed to apply field '{}': {}", fieldName, e.getMessage());
                skipped++;
            }
        }

        FeatureFlags.update();
        eu.midnightdust.lib.config.MidnightConfig.write(ScaleMe.MOD_ID);

        return "§aApplied " + applied + " setting" + (applied == 1 ? "" : "s")
                + (skipped > 0 ? " §7(" + skipped + " skipped)" : "") + ".";
    }

    /** Builds a compact JSON string for the given category (or all categories if null). */
    private static String buildExportJson(String category) {
        JsonObject root = new JsonObject();
        root.addProperty("v", SCHEMA_VERSION);
        if (category != null) {
            root.addProperty("cat", category);
        }

        JsonObject data = new JsonObject();
        if (category == null) {
            for (Map<String, Object> catMap : DEFAULTS.values()) {
                addDifferences(data, catMap);
            }
        } else {
            Map<String, Object> catMap = DEFAULTS.get(category);
            if (catMap != null) {
                addDifferences(data, catMap);
            }
        }
        root.add("d", data);

        return compactJson(root);
    }

    /** Compares current config values against defaults and adds only non-defaults to the JSON. */
    private static void addDifferences(JsonObject out, Map<String, Object> defaults) {
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            String name = entry.getKey();
            Object def = entry.getValue();
            Object current = getFieldValue(name);
            if (current == null) continue;
            if (!valuesEqual(current, def)) {
                if (current instanceof Boolean) {
                    out.addProperty(name, (Boolean) current);
                } else if (current instanceof Float) {
                    out.addProperty(name, (Float) current);
                } else if (current instanceof Number) {
                    out.addProperty(name, ((Number) current).floatValue());
                }
            }
        }
    }

    /** Returns a short human preview like "Item Animation" or "All settings". */
    private static String buildPreview(String category) {
        if (category == null) return "§7All settings";
        return switch (category) {
            case ScaleMeConfig.HAND -> "§7Held Item";
            case ScaleMeConfig.ANIM -> "§7Item Animation";
            case ScaleMeConfig.SCALE -> "§7Entity Scale";
            case ScaleMeConfig.VIEW -> "§7Camera & Crosshair";
            case ScaleMeConfig.ITEM -> "§7Dropped Items";
            default -> "§7Custom";
        };
    }

    /** Reads a static field from {@link ScaleMeConfig} via reflection. */
    private static Object getFieldValue(String name) {
        try {
            Field field = ScaleMeConfig.class.getDeclaredField(name);
            return field.get(null);
        } catch (ReflectiveOperationException e) {
            ScaleMe.LOGGER.error("Failed to read config field '{}': {}", name, e.getMessage());
            return null;
        }
    }

    /** Writes a JSON value into a static {@link ScaleMeConfig} field. */
    private static void applyField(Field field, JsonElement value) throws IllegalAccessException {
        Class<?> type = field.getType();
        if (type == boolean.class || type == Boolean.class) {
            field.setBoolean(null, value.getAsBoolean());
        } else if (type == float.class || type == Float.class) {
            field.setFloat(null, value.getAsFloat());
        } else if (type == int.class || type == Integer.class) {
            field.setInt(null, value.getAsInt());
        } else if (type == double.class || type == Double.class) {
            field.setDouble(null, value.getAsDouble());
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + type.getName());
        }
    }

    /** Loose equality for Float vs the Integer/Float defaults stored in the map. */
    private static boolean valuesEqual(Object a, Object b) {
        if (a == null || b == null) return a == b;
        if (a instanceof Number na && b instanceof Number nb) {
            return Float.compare(na.floatValue(), nb.floatValue()) == 0;
        }
        return a.equals(b);
    }

    /** Emits JSON without any unnecessary whitespace. */
    private static String compactJson(JsonObject obj) {
        StringWriter sw = new StringWriter();
        try (JsonWriter jw = new JsonWriter(sw)) {
            jw.setIndent("");
            jw.setLenient(false);
            GSON.toJson(obj, jw);
        } catch (IOException e) {
            // StringWriter never throws IOException
            throw new AssertionError(e);
        }
        return sw.toString();
    }
}
