package com.github.scaleme.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ScaleMeConfig extends MidnightConfig {
    public static final String SCALING = "scaling";
    public static final String OTHER_PLAYERS = "other_players";
    public static final String PLAYER_PRESETS = "player_presets";

    @Comment(category = SCALING, name = "Change the visual size of your own player model")
    public static Comment ownPlayerDescription;

    @Entry(category = SCALING, name = "Own Player Scale", isSlider = true, min = 0.1f, max = 3.0f)
    public static float ownPlayerScale = 1.0f;

    @Entry(category = SCALING, name = "Smooth Scaling for Own Player")
    public static boolean ownPlayerSmoothScaling = true;

    @Comment(category = OTHER_PLAYERS, name = "Change the visual size of other players' models")
    public static Comment otherPlayersDescription;

    @Entry(category = OTHER_PLAYERS, name = "Enable Other Players Scaling")
    public static boolean enableOtherPlayersScaling = false;

    @Entry(category = OTHER_PLAYERS, name = "Other Players Scale", isSlider = true, min = 0.1f, max = 3.0f)
    public static float otherPlayersScale = 1.0f;

    @Entry(category = OTHER_PLAYERS, name = "Smooth Scaling for Other Players")
    public static boolean otherPlayersSmoothScaling = true;

    @Entry(category = OTHER_PLAYERS, name = "Apply to All Players (including own)")
    public static boolean applyToAllPlayers = false;

    @Comment(category = PLAYER_PRESETS, name = "Create custom scaling profiles for specific players")
    public static Comment playerPresetsDescription;

    @Entry(category = PLAYER_PRESETS, name = "Enable Player Presets")
    public static boolean enablePlayerPresets = true;

    /*
    @Entry(category = PLAYER_PRESETS, name = "Show Quick Add Button in Chat")
    public static boolean showQuickAddButton = true;

    @Entry(category = PLAYER_PRESETS, name = "Auto-detect UUIDs")
    public static boolean autoDetectUUIDs = true;
    */
}