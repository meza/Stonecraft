# Stonecraft

Stonecraft is a configuration Gradle plugin that removes the boilerplate of setting up a **multi-loader, multi-version** Minecraft modding workspace.
It uses [Stonecutter][stonecutter] and [Architectury][architectury] to provide the multi-loader, multi-version support.

Stonecraft reduces about 500 lines of copy-paste build.gradle.kts to a single line with a tested and versioned plugin.

For the full documentation, please visit [Stonecraft](https://stonecraft.meza.gg)

## Quickstart

### Generate a Stonecraft project

The simplest way to start a new mod is with the [Stonecraft project generator](https://stonecraft.meza.gg/generator).
It creates the complete workspace locally in your browser.
The generator keeps the form state in the page URL, so you can copy the address to share a setup.
The URL contains those project details, so share it only with people who should be able to read them.

Download the ZIP, unzip it into a new directory, and follow the generated README.

If you generated a project, stop here. The remaining steps are only for adding Stonecraft to an existing Architectury project.

### Add the plugin to build.gradle[.kts]

If you already use Architectury, make sure to add Stonecraft BEFORE the Architectury plugin.

```kotlin
// build.gradle.kts
plugins {
   id("gg.meza.stonecraft") 
}
```

For available prerelease builds, follow the [snapshot channel guide](https://stonecraft.meza.gg/docs/stonecutter-compatibility#experimental-snapshot-channel). A snapshot may not exist for the current release line.

### If you're using an AI coding agent:

Run this to install a Stonecraft and Stonecutter agent skill

```shell
./gradlew installStonecraftSkill
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
   id("gg.meza.stonecraft") version "1.14.+"
    id("dev.kikugie.stonecutter") version "0.9+"
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
        mc("1.21.3", "fabric", "forge", "neoforge")
        mc("1.21.4", "fabric", "forge", "neoforge")

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

When IntelliJ imports the Gradle project, Stonecraft generates the **Run the Active Minecraft Client** run
configuration. Run it normally to launch the active client, or start it with **Debug** to attach the
IntelliJ debugger to the Minecraft process. See the
[Quickstart guide](https://stonecraft.meza.gg/docs/Quickstart#run-or-debug-the-active-client-in-intellij)
for details.

## Social?

Please join our [discord] server for any questions, and help us make Stonecraft better.

---

[stonecutter]: https://stonecutter.kikugie.dev/
[architectury]: https://docs.architectury.dev/
[github]: https://github.com/meza/Stonecraft
[dicord]: https://discord.gg/dvg3tcQCPW
