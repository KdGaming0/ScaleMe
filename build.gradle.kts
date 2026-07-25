plugins {
    // This plugin applies the correct loom variant based on the Minecraft version
    id("dev.kikugie.loom-back-compat")
    `maven-publish`
    id("me.modmuss50.mod-publish-plugin")
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

// This can be used for publishing on Modrinth and Curseforge
val compatibleVersions: List<String> =
    sc.properties.rawOrNull("mod", "mc_releases")?.asList().orEmpty().map { it.toString() }

repositories {
    mavenCentral()
    /*
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    exclusiveContent {
        forRepository {
            maven {
                url = uri("https://maven.azureaaron.net/releases")
            }
        }
        filter {
            includeGroup("net.azureaaron")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Applies Mojang Mappings
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    modImplementation("maven.modrinth:midnightlib:${property("deps.midnightlib_version")}")
    include("maven.modrinth:midnightlib:${property("deps.midnightlib_version")}")

    modImplementation("net.azureaaron:hm-api:${property("deps.hm_api_version")}")
    include("net.azureaaron:hm-api:${property("deps.hm_api_version")}")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
    modRuntimeOnly("maven.modrinth:modmenu:${property("deps.modmenu_version")}")
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_compat"))
        inputs.property("fabricloader", project.property("deps.fabric_loader"))
        inputs.property("midnightlib", project.property("deps.midnightlib_version"))
        inputs.property("fabric_api", project.property("deps.fabric_api"))
        inputs.property("hm_api", project.property("deps.hm_api_version"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_compat"),
            "fabricloader" to project.property("deps.fabric_loader"),
            "midnightlib" to project.property("deps.midnightlib_version"),
            "fabric_api" to project.property("deps.fabric_api"),
            "hm_api" to project.property("deps.hm_api_version"),
        )

        filesMatching("fabric.mod.json") { expand(props) }
    }

    jar {
        from("LICENSE") {
            rename { fileName -> "${fileName}_${project.property("mod.id")}" }
        }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/
    register<Copy>("buildAndCollect") {
        group = "build"
        // loomx.mod(Sources)Jar returns the jar task for the applied loom variant
        from(loomx.modJar.map { it.archiveFile }, loomx.modSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

if (sc.current.version in compatibleVersions) {
    val changelogFile = rootProject.file("CHANGELOG.md")
    val publishChangelog = if (changelogFile.exists()) changelogFile.readText() else "No changelog provided."

    publishMods {
        file.set(loomx.modJar.flatMap { it.archiveFile })
        additionalFiles.from(loomx.modSourcesJar.flatMap { it.archiveFile })

        displayName.set("${property("mod.name")} v${property("mod.version")} for mc${sc.current.version}")
        version.set("v${property("mod.version")}-mc${sc.current.version}")
        changelog.set(publishChangelog)
        type.set(BETA)
        modLoaders.add("fabric")

        dryRun.set(providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null)

        val modrinthId = providers.gradleProperty("publish.modrinth").orNull
        if (!modrinthId.isNullOrEmpty()) {
            modrinth {
                projectId.set(modrinthId)
                accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
                minecraftVersions.addAll(compatibleVersions)
                requires { slug = "P7dR8mSH" } // Fabric API
                optional { slug = "mOgUt4GM" } // ModMenu
                embeds   { slug = "codAaoxh" } // MidnightLib
            }
        }

        val curseforgeId = providers.gradleProperty("publish.curseforge").orNull
        if (!curseforgeId.isNullOrEmpty()) {
            curseforge {
                projectId.set(curseforgeId)
                accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
                minecraftVersions.addAll(compatibleVersions)
                client.set(true)
                requires { slug = "fabric-api" } // Fabric API
                optional { slug = "modmenu" } // ModMenu
                embeds   { slug = "midnightlib" } // MidnightLib
            }
        }
    }
}