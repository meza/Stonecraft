---
title: modSettings
---
## Main Configuration

Below are all the settings that Stonecraft currently supports as a plugin.

As you can see, there's not much going on here. All the plugin is doing, is making sure that the underlying systems
are configured in a consistent manner.

:::info

All the settings are optional, and you can override them if you want to.

:::

### All modSettings

```kotlin
// build.gradle.kts
plugins {
    id("gg.meza.stonecraft")
}

modSettings {
    clientOptions {
        additionalLines = mapOf<String, String>()
        darkBackground = false
        fov = 70
        guiScale = 3
        musicVolume = 1.0
        narrator = false
    }

    generatedResources = project.layout.projectDirectory.dir("src/main/generated")

    fabricClientJunitReportLocation = project.layout.buildDirectory.file("junit.xml")
    fabricServerJunitReportLocation = project.layout.buildDirectory.file("junit.xml")

    runDirectory = rootProject.layout.projectDirectory.dir("run")
    testClientRunDirectory = rootProject.layout.projectDirectory.dir("run")
    testServerRunDirectory = rootProject.layout.projectDirectory.dir("run")

    variableReplacements = mapOf<String, Any>()
}
```
