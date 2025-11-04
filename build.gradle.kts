plugins {
    id("fabric-loom") version "1.13-SNAPSHOT"
}

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.wispforest.io/releases/")
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings("net.fabricmc:yarn:${property("deps.yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    modImplementation("maven.modrinth:midnightlib:${property("deps.midnightlib_version")}")
    include("maven.modrinth:midnightlib${property("deps.midnightlib_version")}")

    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu_version")}")
    modImplementation("io.wispforest:owo-lib:${property("deps.owo_version")}")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")

    implementation("com.google.code.gson:gson:2.10.1")
}

// Add this mixin configuration block
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
    val java = if (stonecutter.eval(stonecutter.current.version, ">=1.20.5"))
        JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}

tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep")
        )

        filesMatching("fabric.mod.json") {
            expand(props)
        }
    }

    // Configure the 'jar' task in Kotlin DSL
    jar {
        from("LICENSE") {
            rename { fileName -> "${fileName}_${base.archivesName.get()}" }
        }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(
            remapJar.map { it.archiveFile },
            remapSourcesJar.map { it.archiveFile }
        )
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

/*
publishMods {
    file = tasks.remapJar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile.get() })
    displayName = "${property("mod.name")} ${property("mod.version")} for ${stonecutter.current.version}"
    version = property("mod.version") as String
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null
        || providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.add(stonecutter.current.version)
        requires {
            slug = "fabric-api"
        }
    }

    curseforge {
        projectId = property("publish.curseforge") as String
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        minecraftVersions.add(stonecutter.current.version)
        requires {
            slug = "fabric-api"
        }
    }
}
*/
/*
publishing {
    repositories {
        maven("...") {
            name = "..."
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${property("mod.group")}.${property("mod.id")}"
            artifactId = property("mod.version") as String
            version = stonecutter.current.version

            from(components["java"])
        }
    }
}
*/