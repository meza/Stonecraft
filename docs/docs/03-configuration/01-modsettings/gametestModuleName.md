# gametestModuleName

| Key                      | Type     | Default               |
|--------------------------|----------|-----------------------|
| `gametestModuleName`     | `String` | `<mod id>_gametest`   |

`gametestModuleName` identifies the test-only mod declared by the Fabric, Forge and NeoForge GameTest manifests. Stonecraft makes the value available as `${gametestModuleName}` while processing JSON, TOML and `pack.mcmeta` resources. Using that replacement in each test descriptor keeps the setting and manifest IDs together; a hard-coded manifest ID can still drift.

Stonecraft does not apply separate mod-ID validation to this setting. Use an ID accepted by every loader configured in the project; when loaders impose different rules, use the shared subset of their allowed formats. If the replacement is not present in a resource, the configured value has no effect on that resource.

The default is the production mod ID followed by `_gametest`. Override it once in `modSettings` when the test mod needs a different ID:

```kotlin title="build.gradle.kts"
modSettings {
    gametestModuleName = "example_integration_tests"
}
```

Use the replacement in each descriptor under `src/gametestModule/resources`, for example in `fabric.mod.json`:

```json
{
  "schemaVersion": 1,
  "id": "${gametestModuleName}",
  "entrypoints": {
    "fabric-gametest": [
      "com.example.gametest.ExampleGameTests"
    ]
  }
}
```
