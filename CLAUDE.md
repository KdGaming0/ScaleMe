# CLAUDE.md

**Any agent (Claude Code or otherwise) working in this repo MUST read this file in full before touching build config, `stonecutter.properties.toml`, `settings.gradle.kts`, or any per-version code.** It documents the multi-version build setup and the Minecraft 26.2 porting requirements. Skipping it risks breaking a version that already builds.

## Project shape

Fabric mod "Scale Me" (`mod.id = scaleme`), built with Stonecutter (multi-version Gradle overlay) + Fabric Loom.

- `settings.gradle.kts` — declares Stonecutter versions: `1.21.10`, `1.21.11`, `26.1` (active). Foojay toolchain resolver is enabled here, so Gradle auto-downloads whatever JDK each version's toolchain block asks for.
- `stonecutter.properties.toml` — centralized per-version properties (`mod.mc_compat`, `mod.mc_releases`, `deps.fabric_api`, `deps.midnightlib_version`, `deps.hm_api_version`, `deps.modmenu_version`). `deps.fabric_loader` and `loomx.loom_version` are global (top of file), not per-version.
- `build.gradle.kts` — per-version required Java is tiered:
  - `>= 26.1` → Java 25
  - `>= 1.20.5` → Java 21
  - else → Java 17
  Toolchain vendor is pinned to Adoptium.
- `stonecutter.gradle.kts` — source replacements gated by version, e.g. `ResourceLocation`→`Identifier` at `>=1.21.11`, `classTweaker v1 named`→`official` at `>=26.1`.

## Local environment (already set up)

- Gradle 9.5.1 requires the daemon JVM itself to be 17+. Temurin JDK 21 is installed and set as `JAVA_HOME` (User env var, since this machine has no admin rights for Machine-scope).
- Per-version compile toolchains (17/21/25) are auto-fetched by the Foojay resolver — don't need to hand-install those.

## Minecraft 26.2 ("Chaos Cubed", released 2026-06-16)

Not yet added to this project's version list. Notes for when it is:

### Gameplay (context only, not code-relevant)
Sulfur caves biome, sulfur cube passive mob, cinnabar/sulfur block sets, experimental Vulkan renderer (toggle, OpenGL still default for now), Java Edition friends-list UI.

### Fabric toolchain requirements
- Fabric Loom **1.17+** (project currently pins `loomx.loom_version = 1.16-SNAPSHOT` — must bump)
- Fabric Loader **0.19.3** stable (project currently pins `deps.fabric_loader = 0.19.2` globally — bumping affects all versions, confirm 0.19.3 is back-compatible with 1.21.10/1.21.11 before bumping globally, or make it per-version if not)
- Gradle 9.5.1 — already satisfied
- Java 25 — already satisfied by the existing `>= 26.1` tier in `build.gradle.kts` (26.2 parses as `>= 26.1`, no build.gradle.kts change needed)

### Breaking API changes vs 26.1
- `Minecraft.getInstance().setScreen()` moved to `Minecraft.getInstance().gui.setScreen()` — grep the codebase for `.setScreen(` before compiling against 26.2.
- Registration lookup restructured: `valueLookupBuilder` removed, replaced by separate `BlockIds`, `BlockItemIds`, `ItemIds` classes — grep for `valueLookupBuilder`.
- Raw OpenGL calls (outside Blaze3D API) need migration for Vulkan backend compatibility — only relevant if this mod does any direct rendering/GL calls.
- New enum-extension API via Fabric Mixin + classtweaker (Loader 0.19.0+ / Loom 1.17+) — may simplify any enum-patching mixins already in place.
- Fabric API 0.150.1 adds tag-entry removal (`"fabric:remove"`) and experimental fluid-entity interaction (`FluidBehaviour`).
- Fabric API 0.152.0 adds `.requires(FabricClientCommandSource::attended)` for client commands.

### Adding the version to this project
1. `settings.gradle.kts` — add `version("26.2", "26.2.x")` inside the `stonecutter { create(rootProject) { ... } }` block (pick the actual patch once released/stable), consider bumping `vcsVersion` once 26.2 is the primary target.
2. `stonecutter.properties.toml` — add a `["26.2"]` table: `mod.mc_compat`, `mod.mc_releases`, `deps.fabric_api` (check the Fabric API changelog for the version tagged `+26.2`), `deps.midnightlib_version`, `deps.hm_api_version`, `deps.modmenu_version` (check each dependency's own 26.2-compatible release).
3. Bump `loomx.loom_version` to `1.17` (or the latest stable 1.17.x) and `deps.fabric_loader` to `0.19.3` — verify both still work for the older versions before committing.
4. Run the grep checks above (`.setScreen(`, `valueLookupBuilder`) against the whole codebase, not just new code, since these are removed/relocated APIs.
5. Build each version individually via Stonecutter's per-version tasks before assuming a global build pass means all versions are fine.

Sources: [Fabric for Minecraft 26.2](https://fabricmc.net/2026/06/15/262.html), [Fabric porting guide](https://docs.fabricmc.net/develop/porting/), [Minecraft Java Edition 26.2](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-2), [Minecraft Wiki: Java Edition 26.2](https://minecraft.wiki/w/Java_Edition_26.2)
