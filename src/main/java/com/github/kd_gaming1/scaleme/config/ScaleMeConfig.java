package com.github.kd_gaming1.scaleme.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ScaleMeConfig extends MidnightConfig {
    public static final String SCALING = "scaling";
    public static final String ITEMSCALING = "item scaling";
    public static final String VIEW = "view";

    // Player Scaling
    @Comment(category = SCALING, name = "Adjust the visual size of your own player model")
    public static Comment ownPlayerDescription;

    @Entry(category = SCALING, name = "Enable Own Player Scaling")
    public static boolean enableOwnPlayerScaling = false;

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

    // Item Scaling

    @Comment(category = ITEMSCALING, name = "Adjust the visual size of items and there held positions and animation")
    public static Comment itemScalingDescription;

    @Entry(category = ITEMSCALING, name = "Enable Item Scale & Position")
    public static boolean enableItemScaleAndPosition = false;

    /*
    TODO: Add option to only affect weapons and tools
    @Entry(category = ITEMSCALING, name = "Only affect weapons and tools")
    public static boolean onlyAffectWeaponsAndTools = false;
     */

    @Entry(category = ITEMSCALING, name = "Item Scale", isSlider = true, min = 0.05f, max = 3.0f)
    public static float itemScale = 1.0f;

    @Comment(category = ITEMSCALING, name = "Adjust the position of your held item")
    public static Comment heldItemPositionDescription;

    @Entry(category = ITEMSCALING, name = "Held Item X Position", isSlider = true, min = -2.0f, max = 2.0f)
    public static float heldItemXPosition = 0.0f;

    @Entry(category = ITEMSCALING, name = "Held Item Y Position", isSlider = true, min = -2.0f, max = 2.0f)
    public static float heldItemYPosition = 0.0f;

    @Entry(category = ITEMSCALING, name = "Held Item Z Position", isSlider = true, min = -2.0f, max = 2.0f)
    public static float heldItemZPosition = 0.0f;

    @Comment(category = ITEMSCALING, name = "Adjust the Rotation of held item")
    public static Comment heldItemRotationDescription;

    @Entry(category = ITEMSCALING, name = "Held Item Yaw Rotation", isSlider = true, min = -180.0f, max = 180.0f)
    public static float heldItemYawRotation = 0.0f;

    @Entry(category = ITEMSCALING, name = "Held Item Pitch Rotation", isSlider = true, min = -90.0f, max = 90.0f)
    public static float heldItemPitchRotation = 0.0f;

    @Entry(category = ITEMSCALING, name = "Held Item Roll Rotation", isSlider = true, min = -180.0f, max = 180.0f)
    public static float heldItemRollRotation = 0.0f;

    @Comment(category = ITEMSCALING, name = "Adjust swing/animation speed of items")
    public static Comment itemAnimationSpeedDescription;

    @Entry(category = ITEMSCALING, name = "Enable Item Swing Modifications")
    public static boolean enableItemSwingModifications = false;

    @Entry(category = ITEMSCALING, name = "Ignore mining effects")
    public static boolean ignoreMiningEffects = true;

    @Entry(category = ITEMSCALING, name = "Disable swing animation bobbing")
    public static boolean disableSwingAnimationBobbing = true;

    @Entry(category = ITEMSCALING, name = "Item Animation Speed", isSlider = true, min = 0.05f, max = 3.0f)
    public static float itemAnimationSpeed = 1.0f;

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