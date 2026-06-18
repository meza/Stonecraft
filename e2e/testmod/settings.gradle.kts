pluginManagement {
    includeBuild("../..")

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
    }
}
plugins {
    id("gg.meza.stonecraft")
    id("dev.kikugie.stonecutter") version "0.9.6"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(
            version: String,
            vararg loaders: String,
        ) {
            // Make the relevant version directories named "1.20.2-fabric", "1.20.2-forge", etc.
            for (it in loaders) version("$version-$it", version)
        }

        mc("26.1", "fabric", "neoforge")
        mc("1.20.4", "fabric", "neoforge")

        vcsVersion = "26.1-fabric"
    }
    create(rootProject)
}

rootProject.name = "StonecraftTestMod"
