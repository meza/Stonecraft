# additionalLines

| Key               | Type                  | Default Value |
|-------------------|-----------------------|---------------|
| `additionalLines` | `Map<String, String>` | `mapOf()`     |

The `additionalLines` setting is a map of additional lines that you can add to the `options.txt` file. 
This setting is useful for adding custom settings that are not supported by Stonecraft.

```kotlin title='build.gradle.kts'
modSettings {
    clientOptions {
        additionalLines = mapOf(
            "fullscreen" to "true",
            "bobView" to "false",
            "pauseOnLostFocus" to "false",
            "modelPart_cape:true" to "false"
        )
    }
}
```

Stonecraft does not do any validation on the values you provide in the `additionalLines` setting.
It is up to you to ensure that the values are correct and that they are supported by the game.

You can find the list of all valid options [on the Minecraft Wiki](https://minecraft.wiki/w/Options.txt)
