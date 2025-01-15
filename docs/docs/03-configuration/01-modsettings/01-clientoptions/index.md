

# clientOptions

Client Options directly refer to the `options.txt` of Minecraft.
This is where you can set the settings you want your development Minecraft clients to start up with.

No more getting blasted by the Narrator or by the red background of the main menu.

Some named items refer to the most generic settings people would want to set.

## Setting up clientOptions

In your [`modSettings`](./) block, you can set up the `clientOptions` block like so:

```kotlin title='build.gradle.kts'
modSettings {
    // highlight-next-line
    clientOptions {}
}
```
