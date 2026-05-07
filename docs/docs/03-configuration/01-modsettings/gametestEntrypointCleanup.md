# gametestEntrypointCleanup

| Key                         | Type      | Default |
|-----------------------------|-----------|---------|
| `gametestEntrypointCleanup` | `Boolean` | `true`  |

Stonecraft removes Fabric's `fabric-gametest` entrypoint from `fabric.mod.json` during normal build resource processing.
The entrypoint remains available when running GameTest targets.

Set `gametestEntrypointCleanup` to `false` when your build output must keep the `fabric-gametest` entrypoint.

```kotlin title="build.gradle.kts"
modSettings {
    gametestEntrypointCleanup = false
}
```
