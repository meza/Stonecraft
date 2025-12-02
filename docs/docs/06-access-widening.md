# Access Wideners/Transformers

Access widening provides a way to loosen the access limits of classes, methods or fields. This includes making them public, or making them extendable (subclassable).

In Fabric, this would typically be done with **Access wideners**, while NeoForge uses **Access Transformers**. Please refer to their respective docs in order to understand the differences.

Stonecraft leverages Stonecutter, which allows you to define a different access widening file for each version supported by your mod by using variables substitution.

## Setting up

### Fabric - Access wideners

#### Creating the files

In your Fabric mod project, create the needed files in a common folder inside `resources`. This depends on the versions your project is supporting.

```
src.main.resources
└── accesswideners/
    └── 1.19.accesswideners
    └── 1.20.accesswideners
    └── 1.21.accesswideners
```

#### Using string template

Add the string template to `fabric.mod.json`, which will be dynamically replaced later in this guide.

```json title="fabric.mod.json"
{
  ...
  "accessWidener": "accesswideners/${aw_file}",
}
```

#### Replacing template on build/run

If you used Stonecutter before, this is where lies the main difference.

Stonecraft already defines variable replacements in the `processResources` task, so modifying the replaced templates in this task will override all the other variables defined and used by Stonecraft.

Instead, we will use the convenient `variableReplacements` properties in `modSettings` to add a new variable replacement. You can read about [variableReplacements](configuration/modsettings/variableReplacements) in Stonecraft.

In your `build.gradle(.kts)` file, you can add the following and edit it as needed for your use case:

```kotlin title="build.gradle.kts"
val minecraft = stonecutter.current.version
val accesswidener = when {
    stonecutter.eval(minecraft, ">=1.21.10") -> "1.21.10.accesswidener"
    stonecutter.eval(minecraft, ">=1.20") -> "1.20.1.accesswidener"
    else -> "1.20.1.accesswidener"
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/accesswideners/$accesswidener")
}
```

Here is what is happening:

- `stonecutter.eval(minecraft, ">=1.21.10") -> "1.21.10.accesswidener"` defines a condition. In this case, if `stonecutter.minecraft` is equal or above `1.21.10`, the file `1.21.10.accesswidener` will be loaded.
- In the `loom` section, we define the variable `accessWidenerPath` to the according file using the previously defined `$accesswidener` variable.

:::tip
Please note, in the `when` loop, the versions are sorted in descending order, as we use the condition `>=`.
Make sure to modify the versions accordingly when necessary.
:::

The last part to add to the file is in the `modSettings` section:

```kotlin title="build.gradle.kts"
modSettings {
    clientOptions {
        ...
    }

    variableReplacements.put("aw_file", accesswidener);
}
```

This will use the previously assigned value of `accesswidener` as the `aw_file` variable. We have previously set it up in our `fabric.mod.json` above ([Using String Templates](#using-string-template)).

Building the project should run the necessary tasks in order to apply your access widening.

### Forge NeoForge - Access Transformers

#### Creating the files

In your NeoForge mod project, create the needed files in a common folder inside `resources`. This depends on the versions your project is supporting.

```
src.main.resources
└── accesstransformers/
    └── 1.20.cfg
    └── 1.21.cfg
```

#### Using string template

Add the string template to `neoforge.mods.toml`, which will be dynamically replaced later in this guide.

```toml title="neoforge.mods.toml"
[[accessTransformers]]
file="accesstransformers/${at_file}"
```

#### Replacing template on build/run

If you used Stonecutter before, this is where lies the main difference.

Stonecraft already defines variable replacements in the `processResources` task, so modifying the replaced templates in this task will override all the other variables defined and used by Stonecraft.

Instead, we will use the convenient `variableReplacements` properties in `modSettings` to add a new variable replacement. You can read about [variableReplacements](configuration/modsettings/variableReplacements) in Stonecraft.

In your `build.gradle(.kts)` file, you can add the following and edit it as needed for your use case:

```kotlin title="build.gradle.kts"
val minecraft = stonecutter.current.version
val accesstransformer = when {
    stonecutter.eval(minecraft, ">=1.21") -> "1.21.cfg"
    stonecutter.eval(minecraft, ">=1.20") -> "1.20.cfg"
    else -> "1.20.cfg"
}

minecraft {
    accessTransformers {
        file("../../src/main/resources/accesstransformers/$accesstransformer")
    }
}
```

Here is what is happening:

- `stonecutter.eval(minecraft, ">=1.21") -> "1.21.cfg"` defines a condition. In this case, if `stonecutter.minecraft` is equal or above `1.21`, the file `1.21.cfg` will be loaded.
- In the `minecraft` section, we define the configuration variable `accessTransformers` to the according file using the previously defined `$accesstransformer` variable.

:::tip
Please note, in the `when` loop, the versions are sorted in descending order, as we use the condition `>=`.
Make sure to modify the versions accordingly when necessary.
:::

The last part to add to the file is in the `modSettings` section:

```kotlin title="build.gradle.kts"
modSettings {
    clientOptions {
        ...
    }

    variableReplacements.put("at_file", accesstransformer);
}
```

This will use the previously assigned value of `accesstransformer` as the `at_file` variable. We have previously set it up in our `neoforge.mods.toml` above ([Using String Templates](#using-string-template)).

Building the project should run the necessary tasks in order to apply your access widening.
