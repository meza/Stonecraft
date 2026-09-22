# gametestEntrypointCleanup

| Key                         | Type      | Default |
|-----------------------------|-----------|---------|
| `gametestEntrypointCleanup` | `Boolean` | `true`  |

This compatibility setting applies when `src/main/resources/fabric.mod.json` declares Fabric's `fabric-gametest` entrypoint. With the default value, Stonecraft removes that entrypoint from the processed production descriptor used in the normal Fabric JAR, but retains it when processing a GameTest target.

For the isolated setup, put the entrypoint in `src/gametestModule/resources/fabric.mod.json`. Test-module resources are not included in production JARs, so they do not require cleanup.

Set `gametestEntrypointCleanup` to `false` only when the production Fabric JAR must keep the `fabric-gametest` entrypoint. That production descriptor will then name test code, so the code must also be present and safe to load in production.

```kotlin title="build.gradle.kts"
modSettings {
    gametestEntrypointCleanup = false
}
```
