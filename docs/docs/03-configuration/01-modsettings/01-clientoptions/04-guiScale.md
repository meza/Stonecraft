# guiScale

| Key        | Type  | Default Value |
|------------|-------|---------------|
| `guiScale` | `Int` | `3`           |

The `guiScale` setting is used to set the GUI scale of the game. The GUI scale is the size of the game's user interface. The default value is `3`.

This setting translates to the `guiScale` setting in the `options.txt` file.

```kotlin title='build.gradle.kts'
modSettings {
    clientOptions {
        guiScale = 3
    }
}
```
