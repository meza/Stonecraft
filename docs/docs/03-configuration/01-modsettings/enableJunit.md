# enableJunit

| Key           | Type      | Default |
|---------------|-----------|---------|
| `enableJunit` | `Boolean` | `true`  |

Stonecraft configures ordinary unit tests to use JUnit Jupiter 5.10.2 and JUnit Platform 1.10.2. It also calls
`useJUnitPlatform()` for every Gradle `Test` task.

Where the selected loader version provides its own JUnit integration, Stonecraft adds that integration too:

| Loader version | Configuration |
|----------------|---------------|
| Fabric Loader before `0.14.15` | JUnit Platform only |
| Fabric Loader `0.14.15` or newer | JUnit Platform and the matching `fabric-loader-junit` version |
| NeoForge before `20.6.122` | JUnit Platform only |
| NeoForge `20.6.122` or newer | JUnit Platform and NeoForge FML test fixtures |
| Forge | JUnit Platform only |

Loader-aware integration initialises the loader facilities intended for unit tests. It does not turn a Gradle `Test` task into
a running Minecraft game. JUnit Platform alone still discovers and runs ordinary Jupiter tests, but it does not provide loader
services. Use GameTests for behavior that needs a running Minecraft environment. Forge does not currently publish an equivalent
loader-aware JUnit integration, so its ordinary unit tests always use JUnit Platform only.

Run the project's normal Gradle test task to exercise the default setup. For a single active project this is usually
`./gradlew test`; use `./gradlew chiseledTest` to run the Stonecutter version matrix. A successful run lists the project's
Jupiter tests in the Gradle test results.

## Taking ownership of JUnit configuration

Set `enableJunit` to `false` when the project must manage its complete test setup:

```kotlin title="build.gradle.kts"
modSettings {
    enableJunit = false
}
```

In this mode Stonecraft does not inspect or change test dependencies, test engines, `Test` tasks, JVM arguments, system
properties, loader fixtures, or validation. Consumer projects may configure any JUnit version or another test framework.

Projects that already declare JUnit or apply loader-specific test workarounds should set `enableJunit = false` while those
settings remain. To migrate to Stonecraft's defaults, audit and remove only configuration now owned by Stonecraft:

- direct `junit-jupiter` and `junit-platform-launcher` dependencies that merely duplicate the defaults;
- `useJUnitPlatform()` calls that exist only to select Jupiter;
- direct `fabric-loader-junit` dependencies;
- NeoForge test-fixture capability wiring, the `java.lang.invoke` module opening, and `fml.modFolders` setup.

Keep project-specific test libraries, dependency constraints, filters, JVM settings, and system properties. Then remove
`enableJunit = false` and run the normal Gradle test task. When Stonecraft manages JUnit, dependency constraints can still
override its default JUnit versions.

The Fabric boundary follows the
[Fabric Loader JUnit introduction](https://github.com/FabricMC/fabric-loader/commit/8765403937197e8b67162dc98cbac9d6ac6eddf4).
NeoForge versions before `20.6.122` do not provide the fixture capability Stonecraft can safely consume; see the
[NeoForge introduction](https://github.com/neoforged/NeoForge/commit/b344310d8ec663f76e4cc67bfb3a743420ab720d).
