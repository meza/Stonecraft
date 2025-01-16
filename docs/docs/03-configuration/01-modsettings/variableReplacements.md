# variableReplacements

| Key                    | Type               | Default    |
|------------------------|--------------------|------------|
| `variableReplacements` | `Map<String, Any>` | emptyMap() |

The `variableReplacements` setting is used to replace variables during the ProcessResources task.

By default, Stonecraft sets this up for `*.json`, `*.toml` and `*.mcmeta` files in the `resources` source set.

## Overriding

You can override this setting if you want to.

```kotlin title="build.gradle.kts"

tasks.processResources {
    // Do whatever you like within the processResources task
}

```

