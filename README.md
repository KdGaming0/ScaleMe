# ScaleMe

[![build](https://github.com/KdGaming0/ScaleMe/actions/workflows/build.yml/badge.svg)](https://github.com/KdGaming0/ScaleMe/actions/workflows/build.yml)

A simple, client-side Minecraft mod that allows you to visually change the scale of your own player model and other players' models.

## Features

*   **Individual Scaling:** Adjust the visual size of your own player model independently from others.
*   **Other Player Scaling:** Enable and control the scale of all other players on a server.
*   **Smooth Transitions:** Toggle smooth, gradual scaling animations for a less jarring experience.
*   **In-Game Configuration:** Easily change settings on the fly using the in-game config screen (powered by [MidnightLib](https://modrinth.com/mod/midnightlib)).
*   **Custom Keybinding:** Open the configuration menu with a configurable key (defaults to `O`).

## Installation

1.  **Install Fabric:** Make sure you have the [Fabric Loader](https://fabricmc.net/use/) installed.
2.  **Install Dependencies:** This mod requires:
    *   [Fabric API](https://modrinth.com/mod/fabric-api)
3.  **Download ScaleMe:** Get the latest version from the project's release page.
4.  **Add to Mods:** Place the downloaded `.jar` files into your `.minecraft/mods` folder.

## Configuration

You can access the configuration screen in two ways:
*   Press the `O` key by default (this can be changed in Minecraft's keybinding settings).
*   Through [Mod Menu](https://modrinth.com/mod/modmenu) if you have it installed.

The following options are available:

*   **Own Player Scale:** A slider to control the size of your character (from 0.1x to 3.0x).
*   **Smooth Scaling for Own Player:** Toggles gradual scale transitions for your character.
*   **Enable Other Players Scaling:** A master switch to turn scaling for other players on or off.
*   **Other Players Scale:** A slider to control the size of other players.
*   **Smooth Scaling for Other Players:** Toggles gradual scale transitions for other players.
*   **Apply to All Players:** When enabled, uses the "Other Players Scale" for everyone, including you.

## Building from Source

1.  Clone the repository: `git clone https://github.com/KdGaming0/ScaleMe.git`
2.  Navigate to the project directory: `cd ScaleMe`
3.  Grant execute permissions to the Gradle wrapper: `chmod +x ./gradlew`
4.  Build the project: `./gradlew build`
5.  The compiled mod `.jar` will be located in the `build/libs/` directory.
