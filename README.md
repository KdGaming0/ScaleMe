# ScaleMe

[![build](https://github.com/KdGaming0/ScaleMe/actions/workflows/build.yml/badge.svg)](https://github.com/KdGaming0/ScaleMe/actions/workflows/build.yml)

**Scale players, NPCs, and even your items in Minecraft.**  
A simple, client-side mod that gives you full control over the size and positioning of player models, NPCs, and the items you hold. Works great for fun with friends, screenshots, or just making the game look the way you want.

### 📦 Download from Modrinth

[![Download on Modrinth](https://img.shields.io/badge/Modrinth-Download-brightgreen?style=for-the-badge&logo=modrinth)](https://modrinth.com/mod/scaleme)

---

## 🛠 Features

### Player & NPC Scaling
- **Individual Player Scaling** – Change your own player size from tiny to towering.
- **Other Player Scaling** – Set the size for all other players on the server.
- **Per-Player Scaling** – Give a specific player a custom size while keeping everyone else normal. *(Default keybind: `P`)*
- **Smooth Transitions** – Optional smooth scaling animations for better visuals.
- **NPC Scaling in Hypixel SkyBlock** – Set a specific scale for NPCs, with a separate slider for villagers used as NPCs (make Jerry tiny or huge).
- **Safeguards for Hypixel** – Automatic limits to prevent scaling from giving unfair advantages in competitive modes.

### Item Scaling & Positioning
- **Item Scaling** – Change the size of the item you hold.
- **Scale Dropped Items** – Adjust the size of items dropped in the world.
- **Position Controls** – Adjust yaw, pitch, roll, and X, Y, Z position in your hand.
- **Swing Speed Control** – Change how fast your swing animation plays.

### View Options
- **Hide Players** – Hide other players from rendering in the world. Useful for crowded lobbies or clean screenshots. Can be restricted to Skyblock only.
- **Third-Person Crosshair (Back View)** – Optional crosshair while in third-person view from behind.
- **Third-Person Crosshair (Front View)** – Optional crosshair while in front-facing third-person view.
- **Disable Selfie Cam** – Remove the front-facing camera entirely if you prefer.

### Configuration
- **In-Game Config Screen** – Change all settings in real time.  
  The config menu is transparent so you can see how your changes affect you or other players’ scale while adjusting them. Especially useful for item scale and position configuration. *(Powered by [MidnightLib](https://modrinth.com/mod/midnightlib))*
- **Config Sharing** – Export and import your configs to easily share your custom scales with friends or across different devices.
  - /scaleme export – Exports your current config.
  - /scaleme import <json> – Imports a config from the export.

---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/).
2. Install required dependencies:
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [oωo (owo-lib)](https://modrinth.com/mod/owo-lib)
3. Download **ScaleMe** and drop it into your `mods` folder.

---

## 💻 Development

### Building from Source
1. Clone the repository: `git clone https://github.com/KdGaming0/ScaleMe.git`
2. Navigate to the project directory: `cd ScaleMe`
3. Grant execute permissions to the Gradle wrapper: `chmod +x ./gradlew`
4. Build the project: `./gradlew build`
5. The compiled mod `.jar` will be located in the `build/libs/` directory.

---

[![Bisect Hosting Banner](https://www.bisecthosting.com/partners/custom-banners/8a1e1ba8-343f-4e31-a276-a1eba99388f3.webp)](https://www.bisecthosting.com/SBE)

## 💻 Need a Minecraft Server?

I’ve partnered with **Bisect Hosting** to offer fast, reliable game servers that work great with modded Minecraft or any other game they host!
Their setup is quick, the hardware is solid, and support is always fast to respond.

Use code **SBE** at checkout for **25% off** your first month:  
👉 [Get a server here](https://www.bisecthosting.com/SBE)

Every server purchased through this link directly supports my work on this mod.
