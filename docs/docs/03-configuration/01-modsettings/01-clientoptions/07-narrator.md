# narrator

| Key        | Type      | Default Value |
|------------|-----------|---------------|
| `narrator` | `Boolean` | `false`       |

The `narrator` option is a boolean that allows you to enable or disable the narrator in Minecraft.

Stonecutter disables this by default to prevent the narrator from interrupting the development flow but if you
are working on something that requires the narration feature, you can enable it by setting this option to `true`.

```kotlin title='build.gradle.kts'
modSettings {
    clientOptions {
        narrator = true
    }
}
```
