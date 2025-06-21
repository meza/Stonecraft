# fov

| Key   | Type  | Default Value |
|-------|-------|---------------|
| `fov` | `Int` | `70`          |

This controls the Field of View (FOV) setting in the options.txt file, and it translates to the `fov` setting.

The reason this setting has its own named parameter is because the value in the `options.txt` file is stored in a different
format. There's a bit of transformation that needs to happen to get the value in degrees to the value the game expects.

:::note
It might not be completely accurate so if you have a better way, please submit a PR.
The current calculation can be found in the [MinecraftClientOptions.kt](https://github.com/meza/Stonecraft/blob/main/src/main/kotlin/gg/meza/stonecraft/data/MinecraftClientOptions.kt) file.
The value stored in `options.txt` ranges from `-1` to `1`. The getter now
converts this value back to degrees using:

```kotlin
fov = ((storedValue + 1) * 40 + 30).toInt()
```
:::

```kotlin title='build.gradle.kts'
modSettings {
    clientOptions {
        fov = 70
    }
}
```
