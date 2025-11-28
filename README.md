# Stonecraft

Stonecraft is a configuration Gradle plugin that removes the boilerplate of setting up a **multi-loader, multi-version** Minecraft modding workspace.
It uses [Stonecutter][stonecutter] and [Architectury][architectury] to provide the multi-loader, multi-version support.

Stonecraft reduces about 500 lines of copy-paste build.gradle.kts to a single line with a tested and versioned plugin.

For the full documentation, please visit [Stonecraft](https://stonecraft.meza.gg)

## Quickstart

### Use the Stonecraft template

The simplest way to get started with a brand new mod project is to use the Stonecraft template.

Simply go to https://github.com/meza/Stonecraft-template and click the "Use this template" button.

### Add the plugin to build.gradle[.kts]

If you already use Architectury, make sure to add Stonecraft BEFORE the Architectury plugin.

```kotlin
// build.gradle.kts
plugins {
   id("gg.meza.stonecraft") 
}
```

### Set up Stonecutter

#### Setting up supported version

Create a `settings.gradle[.kts]` file in your project root with the following content:

This is still boilerplate, and I'm working on making it more user-friendly.

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
    }
}
plugins {
    id("gg.meza.stonecraft") version "1.+"
    id("dev.kikugie.stonecutter") version "0.7.+"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(version: String, vararg loaders: String) {
            // Make the relevant version directories named "1.20.2-fabric", "1.20.2-forge", etc.
            for (it in loaders) vers("$version-$it", version)
        }

        mc("1.20", "fabric", "forge", "neoforge")
        mc("1.21.3", "fabric", "neoforge")
        mc("1.21.4", "fabric", "neoforge")

    }
    create(rootProject)
}

rootProject.name = "YourModName"

```

#### Setting up the stonecutter

Create a `stonecutter.gradle[.kts]` file in your project root with the following content:

```kotlin
// stonecutter.gradle.kts
plugins {
    id("dev.kikugie.stonecutter")
    id("gg.meza.stonecraft")
}

stonecutter active "1.21.4-fabric" /* [SC] DO NOT EDIT */
```

[stonecutter]: https://stonecutter.kikugie.dev/
[architectury]: https://docs.architectury.dev/
[github]: https://github.com/meza/Stonecraft
