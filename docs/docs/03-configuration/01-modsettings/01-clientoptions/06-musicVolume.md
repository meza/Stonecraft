# musicVolume

| Name          | Type    | Default Value |
|---------------|---------|---------------|
| `musicVolume` | `Float` | `1.0`         |

The volume of the music in the game. This is a float value between 0.0 and 1.0. The default value is 1.0.

**0.0 is silent, and 1.0 is the loudest.**

The reason this setting has its own named parameter is purely syntax sugar to mask the `soundCategory_music` key in the 
`options.txt` file.

:::note
If you think that Stonecraft should have shorthands for other sound categories, please submit a PR.
You can find the mappings in the [MinecraftClientOptions.kt](https://github.com/meza/Stonecraft/blob/main/src/main/kotlin/gg/meza/stonecraft/data/MinecraftClientOptions.kt#L54) file
:::
```kotlin title='build.gradle.kts'
modSettings {
    clientOptions {
        musicVolume = 1.0
    }
}
```
