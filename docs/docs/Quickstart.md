
## Quickstart

### Add the plugin to build.gradle[.kts]

If you already use Architectury, make sure to add Stonecraft BEFORE the Architectury plugin.

```kotlin
// build.gradle.kts
plugins {
   id("gg.meza.stonecraft") version "1.0.0"   
}
```

### Set up Stonecutter

#### Setting up supported version

Create a `settings.gradle[.kts]` file in your project root with the following content:

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
    }
}
plugins {
    id("dev.kikugie.stonecutter") version "0.5"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(version: String, vararg loaders: String) {
            // Make the relevant version directories named "1.20.2-fabric", "1.20.2-forge", etc.
            for (it in loaders) vers("$version-$it", version)
        }

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
