package com.github.kd_gaming1.scaleme.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ScaleMeConfig extends MidnightConfig {
    public static final String SCALING = "scaling";
    public static final String VIEW = "view";

    // Own Player Scaling
    @Comment(category = SCALING, name = "Adjust the visual size of your own player model")
    public static Comment ownPlayerDescription;

    @Entry(category = SCALING, name = "Own Player Scale", isSlider = true, min = 0.1f, max = 3.0f)
    public static float ownPlayerScale = 1.0f;

    @Comment(category = SCALING, name = "Adjust the visual size of other players")
    public static Comment playersDescription;

    @Entry(category = SCALING, name = "Enable Scaling for Other Players")
    public static boolean enableOtherPlayersScaling = false;

    @Entry(category = SCALING, name = "Other Players Scale", isSlider = true, min = 0.1f, max = 3.0f)
    public static float otherPlayersScale = 1.0f;

    @Comment(category = SCALING, name = "Adjust the visual size of NPC players")
    public static Comment npcDescription;

    @Entry(category = SCALING, name = "Enable Hypixel NPC Scaling")
    public static boolean enableNpcScaling = false;

    @Entry(category = SCALING, name = "NPC Scale", isSlider = true, min = 0.1f, max = 3.0f)
    public static float npcPlayerScale = 1.0f;

    @Comment(category = SCALING, name = "Adjust the visual size of Villager NPC Scaling")
    public static Comment villagerNpcDescription;

    @Entry(category = SCALING, name = "Enable Hypixel Villager NPC Scaling")
    public static boolean enableVillagerNpcScaling = false;

    @Entry(category = SCALING, name = "Villager NPC Scale", isSlider = true, min = 0.1f, max = 3.0f)
    public static float villagerNpcScale = 1.0f;

    @Comment(category = SCALING, name = "Make changes between scale levels appear smooth")
    public static Comment smoothedScalingDescription;

    @Entry(category = SCALING, name = "Smooth Scaling")
    public static boolean smoothScaling = true;

    /*
    TODO: Add Quick Add Button to chat behind players
    @Entry(category = PLAYERS, name = "Show Quick Add Button")
    public static boolean showQuickAddButton = true;
    */

    // View (Crosshair + Camera)
    @Comment(category = VIEW, name = "Configure crosshair and camera options")
    public static Comment viewDescription;

    @Entry(category = VIEW, name = "Show Crosshair in Third Person (Back)")
    public static boolean enableCrosshairInThirdPerson = false;

    @Entry(category = VIEW, name = "Show Crosshair in Third Person (Front)")
    public static boolean enableCrosshairInThirdPersonFront = false;

    @Entry(category = VIEW, name = "Disable Selfie Cam (Front View)")
    public static boolean disableSelfieCam = false;
}