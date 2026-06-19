# Access Wideners/Transformers

_Since Stonecraft version: 1.12.0_

Access widening provides a way to loosen the access limits of classes, methods or fields. This includes making them public, or making them extendable (subclassable).

In Fabric, this would typically be done with **Access wideners**, while NeoForge uses **Access Transformers**. Please refer to their respective docs in order to understand the differences.

Stonecraft leverages Stonecutter and Architectury Loom, which allows you to define a different access widening file for each version supported by your mod by using variable substitution.

## Setting up

:::important
Stonecraft and Loom will **automatically convert your access wideners to access transformers** for NeoForge and Forge, so you don't need to worry about that. You only need to define the access widener files for Fabric, and Stonecraft will handle the rest.
:::

### Step 1: Creating the files

In your project, create the needed files in a common folder inside `resources`. This depends on the versions your project is supporting.

```
src.main.resources
└── accesswideners/
    └── 1.19.accesswidener
    └── 1.20.accesswidener
    └── 1.21.accesswidener
```

### Step 2: Define the variable

```kotlin title="build.gradle.kts"
val awFile = // This is the main logic to determine which access widener file to use based on the current Minecraft version.
    when {
        stonecutter.current.parsed >= "1.21" -> {
            project.rootProject.layout.projectDirectory
                .file(
                    "src/main/resources/accesswideners/1.21.accesswidener",
                )
        }

        stonecutter.current.parsed >= "1.20" -> {
            project.rootProject.layout.projectDirectory
                .file(
                    "src/main/resources/accesswideners/1.20.accesswidener",
                )
        }
        
        else -> {
            project.rootProject.layout.projectDirectory
                .file(
                    "src/main/resources/accesswideners/1.19.accesswidener",
                )
        }
    }

modSettings {
    
    // ... rest of your modSettings configuration

    accessWidenerLocation = awFile // This tells Stonecraft and Loom which AW to use
}
```

## Opting out of automatic access widener handling

:::warning
This is completely optional and not recommended unless you have very specific needs
:::

In your `build.gradle.kts`, you need to add the following to your `modSettings`:

```kotlin title="build.gradle.kts"

val awFile = // This is the main logic to determine which access widener file to use based on the current Minecraft version.
    when {
        stonecutter.current.parsed >= "1.21"-> {
            when {
                project.mod.isFabric -> project.rootProject.layout.projectDirectory
                    .file(
                        "src/main/resources/accesswideners/1.21.accesswidener",
                    )
                else -> project.rootProject.layout.projectDirectory
                    .file(
                        "src/main/resources/accesstransformers/1.21.cfg",
                    )
            }
        }

        stonecutter.current.parsed >= "1.20" -> {
            when {
                project.mod.isFabric -> project.rootProject.layout.projectDirectory
                    .file(
                        "src/main/resources/accesswideners/1.20.accesswidener",
                    )
                else -> project.rootProject.layout.projectDirectory
                    .file(
                        "src/main/resources/accesstransformers/1.20.cfg",
                    )
            }
        }
        
        else -> {
            when {
                project.mod.isFabric -> project.rootProject.layout.projectDirectory
                    .file(
                        "src/main/resources/accesswideners/1.19.accesswidener",
                    )
                else -> project.rootProject.layout.projectDirectory
                    .file(
                        "src/main/resources/accesstransformers/1.19.cfg",
                    )
            }
        }
    }

modSettings {

    // ... rest of your modSettings configuration
    
    accessWidenerProcessing = false // This tells Stonecraft and Loom to not handle the access widener automatically
    variableReplacements =
        mapOf(
            "awFile" to awFile.asFile
                .relativeTo(
                    project.rootProject.layout.projectDirectory
                        .dir("src/main/resources")
                        .asFile,
                ).invariantSeparatorsPath,
        )
}
```

This will give you access to the `awFile` variable in your metadata files to point them to the correct file.

### Fabric

In your `fabric.mod.json`, you need to add the following:

```json title="fabric.mod.json"
{
  "accessWidener": "${awFile}"
}
```

### NeoForge

In your `META-INF/neoforge.mods.toml`, you need to add the following:

```toml title="META-INF/neoforge.mods.toml"
[[access-transformer]]
file="${awFile}"
```

### Forge

For Forge, you need to rely on Stonecraft/Loom to do the work for you, you can't define the access transformer in your `mods.toml` file.
Please refer to [Step 2](#step-2-define-the-variable)
