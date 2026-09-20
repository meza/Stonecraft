pluginManagement {
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
    id("gg.meza.stonecraft") version "__STONECRAFT_VERSION__"
    id("dev.kikugie.stonecutter") version "__STONECUTTER_VERSION__"
}

stonecutter {
    shared {
        fun mc(
            version: String,
            vararg loaders: String,
        ) {
            // Make the relevant version directories named "1.20.2-fabric", "1.20.2-forge", etc.
            for (it in loaders) version("$version-$it", version)
        }

        mc("26.1"__STONECRAFT_#FABRIC__, "fabric"__STONECRAFT_/FABRIC____STONECRAFT_#FORGE__, "forge"__STONECRAFT_/FORGE____STONECRAFT_#NEOFORGE__, "neoforge"__STONECRAFT_/NEOFORGE__)
        mc("26.2"__STONECRAFT_#FABRIC__, "fabric"__STONECRAFT_/FABRIC____STONECRAFT_#FORGE__, "forge"__STONECRAFT_/FORGE____STONECRAFT_#NEOFORGE__, "neoforge"__STONECRAFT_/NEOFORGE__)
        mc("26.3"__STONECRAFT_#FABRIC__, "fabric"__STONECRAFT_/FABRIC____STONECRAFT_#FORGE__, "forge"__STONECRAFT_/FORGE____STONECRAFT_#NEOFORGE__, "neoforge"__STONECRAFT_/NEOFORGE__)

        vcsVersion = "26.3-__STONECRAFT_PRIMARY_LOADER__"
    }
    create(rootProject)
}

rootProject.name = "__STONECRAFT_MOD_NAME__"
