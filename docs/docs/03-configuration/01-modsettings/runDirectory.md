# runDirectory

| Key            | Type      | Default                                        |
|----------------|-----------|------------------------------------------------|
| `runDirectory` | Directory | rootProject.layout.projectDirectory.dir("run") |

This setting is used to configure the directory where the game will be run from during development. 
The default value is set to the `run` directory in the [`root project`](../index.mdx#project-vs-root-project) directory.

This is also passed through to the [`testClientRunDirectory`](testRunDirectory.md) and [`testServerRunDirectory`](testRunDirectory.md) settings by default.

## Overriding

You can override this setting if you want to.

```kotlin title="build.gradle.kts"
loom {
    runConfigs.all {
        runDir = "../../run" // <- Loom requires a String, and it's relative from the subproject path
    }
}
```

When overriding the runDirectory, make sure to then override the [`testClientRunDirectory`](testRunDirectory.md#overriding) 
and [`testServerRunDirectory`](testRunDirectory.md#overriding) settings as well.
