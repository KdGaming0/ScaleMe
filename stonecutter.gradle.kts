plugins {
    id("dev.kikugie.stonecutter")
    id("me.modmuss50.mod-publish-plugin") version "2.1.+" apply false
}

stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}

stonecutter active "26.1"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
        string(current.parsed < "1.21.11") {
            // Util moved from net.minecraft to net.minecraft.util in 1.21.11
            replace("import net.minecraft.util.Util;", "import net.minecraft.Util;")
        }
        string(current.parsed >= "26.1") {
            replace("classTweaker v1 named", "classTweaker v1 official")
        }
        string(current.parsed < "26.1") {
            // Fabric API renamed the keybinding and client-command helpers in 26.1
            replace(
                "import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;",
                "import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;"
            )
            replace("KeyMappingHelper.registerKeyMapping(", "KeyBindingHelper.registerKeyBinding(")
            replace("ClientCommands", "ClientCommandManager")
            // CameraRenderState moved into the .state.level package in 26.1
            replace(
                "net.minecraft.client.renderer.state.level.CameraRenderState",
                "net.minecraft.client.renderer.state.CameraRenderState"
            )
            replace(
                "net/minecraft/client/renderer/state/level/CameraRenderState",
                "net/minecraft/client/renderer/state/CameraRenderState"
            )
        }
        string(current.parsed >= "26.2") {
            replace("client.setScreen(", "client.gui.setScreen(")
            replace(
                "import net.minecraft.world.entity.EntityType;",
                "import net.minecraft.world.entity.EntityType;\nimport net.minecraft.world.entity.EntityTypes;"
            )
            // 26.2 renamed the first-person render path render* -> submit* (signatures unchanged)
            replace("\"renderArmWithItem\"", "\"submitArmWithItem\"")
            // 26.2 split Gui: HUD rendering moved to the new Hud class, Gui now manages screens
            replace("import net.minecraft.client.gui.Gui;", "import net.minecraft.client.gui.Hud;")
            replace("@Mixin(Gui.class)", "@Mixin(Hud.class)")
        }
        regex(current.parsed >= "26.2") {
            replace(
                "(?<!\\.)\\bclient\\.screen\\b" to "client.gui.screen()",
                "\\bclient\\.gui\\.screen\\(\\)" to "client.screen"
            )
            replace(
                "\\bEntityType\\.([A-Z][A-Z0-9_]*)\\b" to "EntityTypes.$1",
                "\\bEntityTypes\\.([A-Z][A-Z0-9_]*)\\b" to "EntityType.$1"
            )
        }
    }
}

val releaseVersions = listOf("1.21.10", "1.21.11", "26.1", "26.2")

stonecutter tasks {
    order("publishMods")
}

tasks.register("publishToAllPlatforms") {
    group       = "publishing"
    description = "Publish all release groups to Modrinth and CurseForge sequentially."
    dependsOn(releaseVersions.map { ":$it:publishMods" })
}

gradle.projectsEvaluated {
    releaseVersions.zipWithNext().forEach { (prev, next) ->
        project(":$next").tasks.matching { it.name == "publishMods" }.configureEach {
            mustRunAfter(project(":$prev").tasks.matching { it.name == "publishMods" })
        }
    }
}
