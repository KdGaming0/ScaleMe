# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).

## What this is

ScaleMe is a **client-side Fabric mod** for Minecraft that scales player/NPC models and held/dropped items, plus a few camera/crosshair tweaks. It is a single-source, multi-version project built with **Stonecutter**: one `src/` tree is preprocessed into per-version subprojects.

## Build & run

There is no test suite — `build` compiles + remaps all version targets. CI (`.github/workflows/build.yml`) runs `./gradlew build` on JDK 25.

```bash
./gradlew build                    # build every version (1.21.10, 1.21.11, 26.1, 26.2)
./gradlew :26.1:build              # build one version
./gradlew :26.1:runClient          # launch dev client for one version (DevAuth + ModMenu included)
./gradlew :26.1:buildAndCollect    # jar into build/libs/<mod.version>/
./gradlew "Set active project to 1.21.11"   # switch the active version (rewrites versioned comments in src/)
./gradlew "Reset active project"            # back to 26.1 — run before committing
```

Jars land in `versions/<version>/build/libs/`. The `run/` directory is shared across all versions.

Publishing (`publishMods`, `publishToAllPlatforms`) is dry-run unless `MODRINTH_TOKEN` / `CURSEFORGE_TOKEN` are set; changelog text comes from `CHANGELOG.md`.

## Multi-version model (Stonecutter)

- `settings.gradle.kts` declares the version tree; `stonecutter.properties.toml` holds per-version dependency versions and `mod.mc_compat` ranges. **Adding a Minecraft version means editing both**, plus `releaseVersions` in `stonecutter.gradle.kts`.
- `versions/*/` contains only build output — there are no per-version source trees. All version differences live inline in `src/` as Stonecutter comments:
  ```java
  //? if >=26.1 {
  import ...KeyMappingHelper;
  //?} else {
  /*import ...KeyBindingHelper;
   *///?}
  ```
  The active version determines which branch is uncommented on disk. **Never hand-edit the commented-out branch's comment markers** — switch the active version and let Stonecutter rewrite them.
- Global token swaps live in `stonecutter.gradle.kts` `parameters {}`: `ResourceLocation`→`Identifier` for ≥1.21.11, and `classTweaker` naming for ≥26.1.
- Mappings are **Mojang official** (`loomx.applyMojangMappings()`), so class names match Mojang names (`AbstractClientPlayer`, `PoseStack`, `LivingEntityRenderer`), not Yarn.
- Java target follows the MC version: 25 for ≥26.1, 21 for ≥1.20.5.

## Architecture

Entry point `ScaleMe.onInitializeClient()` (`src/main/java/com/github/kd_gaming1/scaleme/ScaleMe.java`) wires everything: MidnightLib config init, Hypixel Mod API packet registration, the sword-block keybind, the per-tick `FeatureFlags.update()`, and command registration.

Everything else is either a **mixin** (render-path hook) or a **util** (state read by mixins). Mixins never read config booleans directly; they read the tick-updated caches.

**Config** — `config/ScaleMeConfig.java` is a MidnightLib `MidnightConfig` subclass: all settings are `public static` fields with `@Entry`/`@Comment` annotations grouped into five categories (`hand_item`, `animation`, `scale`, `view`, `item`). Adding a setting means adding the field *and* its `en_us.json` lang keys (`assets/scaleme/lang/`) — MidnightLib resolves labels/tooltips by field name.

**Hot-path caching** (the reason for the indirection layers):
- `FeatureFlags` — every config boolean collapsed into one `int` bitmask, recomputed once per client tick. Mixins call `FeatureFlags.isEnabled(MASK)` (all bits) or `anyEnabled(MASK)` (any bit) as their first-line early-out. A new toggle needs a new bit constant and a line in `update()`.
- `PerTickCache` — memoizes entity-id → scale for the current tick/level, delegating to `ScaleResolver`.
- `NpcCache` — memoizes the Hypixel NPC check per tick.
- `HandContext` — resolved per-hand item transform (translation/rotation/scale), populated by `ItemInHandRendererMixin` and consumed by `LayerRenderStateMixin`; only valid while `renderDepth > 0`.

**Scale resolution** — `ScaleResolver.resolveScale()` is the single decision point: own player → `playerScale`; Hypixel NPC → `hypixelNpcScale` (forced to 1.0 in dungeons); everyone else → `otherPlayersScale`, but **only outside Hypixel or inside SkyBlock**. This gating is deliberate anti-advantage behavior on Hypixel; preserve it when touching scale logic.

**Hypixel integration** — `HypixelLocationState` subscribes to `LocationUpdateS2CPacket` from `hm-api` (Hypixel Mod API) to track server/SkyBlock/dungeon state, and resets on disconnect. `HypixelNpcUtil` detects NPCs heuristically: fake player entities whose scoreboard team has nametag visibility `NEVER`. Both no-op off Hypixel.

**Mixins** (`scaleme.mixins.json`, all client-side, prefix `scaleme$`):
`ItemInHandRendererMixin` (largest — first-person arm/item transform, swing overrides, sword block pose) · `LayerRenderStateMixin` (applies `HandContext` around item submit) · `AvatarRendererMixin` + `EntityRenderDispatcherMixin` (player scaling, hide-players, own nametag) · `LivingEntityRendererMixin` (villager-as-NPC scale) · `LivingEntityMixin` (swing duration) · `ItemEntityRendererMixin` (dropped items) · `CameraTypeMixin` (selfie cam) · `GuiMixin` (third-person crosshair).

**Presets** — `PresetManager` exports/imports config as compact JSON through the clipboard (no files), scoped by category, with a `SCHEMA_VERSION` and a per-category defaults map that must be kept in sync with `ScaleMeConfig`. Exposed via `/scaleme export [category]` and `/scaleme import <json>` in `command/Commands.java`.

# Development Workflow

## Documentation

- `CLAUDE.md`: Project architecture and design overview.
- `IMPLEMENTATION_LOG.md`: Record of implementation decisions and reasoning. Keep entries concise (maximum 500 lines total).
- Note that `IMPLEMENTATION_LOG.md`, `graphify-out/`, and `.claude/` are gitignored — they're local working context, not part of the published repo, so they won't show up in diffs or PRs.

## Workflow

### 1. Investigate First

Before writing any code, investigate the requested feature or reported issue.

- If any requirement is unclear, **stop and ask for clarification** before proceeding. Do not make assumptions. It is better to clarify early than to implement the wrong solution.
- Explain your findings briefly after the investigation.
- If the investigation naturally splits into independent tasks (for example, tracing a rendering bug, auditing mixin order, and checking overlay lifecycle), suggest running parallel sub-agents, with one agent handling each independent task. Only suggest this when it provides a meaningful speed or quality improvement.

### 2. Present a Plan

After completing the investigation:

- Present a short implementation plan or specification.
- Explain how you intend to solve the problem.
- Wait for explicit approval before writing any code.

### 3. Implement

Once approval has been given:

- Implement the planned changes.
- Keep the implementation focused on the approved scope.
- Avoid unrelated refactoring unless it is required to complete the task safely.

### 4. Validate

Before considering the work complete:

- Run all relevant tests.
- Run:

```bash
./gradlew build
```

to ensure the project builds successfully and all automated checks pass.

If manual in-game testing is required, clearly describe:

- what should be tested
- how to reproduce it
- what the expected result is

Wait for the user to complete manual testing and report back with the results before finalizing the task.

### 5. Documentation

After all testing has passed:

- Add a concise entry to `IMPLEMENTATION_LOG.md` describing:
  - what changed
  - why the change was made
  - any notable implementation decisions

- Run:

```bash
graphify update .
```

to keep the project graph up to date.

- If the work fixes a bug or introduces a user-visible feature, update `CHANGELOG.md` with a short, non-technical description suitable for end users. Avoid implementation details and internal terminology.

## General Principles

- Investigate before implementing.
- Ask for clarification instead of assuming.
- Do not write code before approval.
- Validate all changes before considering the task complete.
- Keep documentation up to date with every completed change.

### Scope Control

Only modify files that are necessary for the requested change. Avoid unrelated formatting changes, refactoring, or file reorganizations unless explicitly requested or required to complete the task safely.

### Preserve Existing Behavior

Unless the request explicitly changes existing functionality, preserve current behavior. If a proposed implementation requires changing existing behavior or introduces trade-offs, explain them in the implementation plan and wait for approval.
