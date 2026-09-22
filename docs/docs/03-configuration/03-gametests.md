# GameTests

Stonecraft provides an isolated `gametest` source set for Fabric, Forge and NeoForge projects. In a Stonecutter workspace, these paths are under the shared project root, alongside `src/main`:

```text
src/gametest/java
src/gametest/resources
src/gametestModule/resources
```

Stonecutter applies the same version and loader preprocessing to these source sets that it applies to `src/main`. Use its conditional source syntax when registration code or descriptors differ between loaders or Minecraft versions.

The `gametest` source set can compile against the production `main` source set. Put loader descriptors in the resource-only `gametestModule` source set; keep GameTest data, structures and test instances under `src/gametest/resources`. The GameTest runs include these outputs, while normal JAR and publishing tasks include only `main`.

## Running GameTests

| Task | Scope and outcome |
| --- | --- |
| `testActiveServer` | Runs `runGameTestServer` for the version-loader pair selected by the `stonecutter active` line. The task fails when a server GameTest fails. |
| `testActiveClient` | Launches `runGameTestClient` for the active pair so client-side GameTests can run. |
| `runGameTestServer` | Stonecutter's aggregate task: runs the server GameTests for every configured pair. |
| `chiseledGameTest` | Backward-compatible alias for the aggregate `runGameTestServer` task. |

For example, run only the active pair with:

```shell
./gradlew testActiveServer
./gradlew testActiveClient
```

Run server GameTests across every configured pair with either aggregate name:

```shell
./gradlew runGameTestServer
./gradlew chiseledGameTest
```

## Fabric test manifest

Fabric discovers the test entrypoint from a test-only mod descriptor at `src/gametestModule/resources/fabric.mod.json`. Give it a different mod ID from the production descriptor and depend on the production mod:

```json
{
  "schemaVersion": 1,
  "id": "${gametestModuleName}",
  "version": "${version}",
  "name": "${name} Game Tests",
  "environment": "*",
  "entrypoints": {
    "fabric-gametest": [
      "com.example.gametest.ExampleGameTests"
    ]
  },
  "depends": {
    "fabricloader": "*",
    "fabric-api": ">=${fabricVersion}",
    "minecraft": ">=${minecraftVersion}",
    "${id}": "*"
  }
}
```

Stonecraft supplies every replacement used above while processing the `gametestModule` resources:

- `${gametestModuleName}` comes from [`modSettings.gametestModuleName`](01-modsettings/gametestModuleName.md) and defaults to `<mod id>_gametest`.
- `${id}`, `${name}` and `${version}` come from the project's `mod` metadata.
- `${fabricVersion}` comes from the active version's `fabric_version` property.
- `${minecraftVersion}` is the effective Minecraft version, including a version-specific override.

Forge and NeoForge also use the separate test module. Add `src/gametestModule/resources/META-INF/mods.toml` for Forge and `src/gametestModule/resources/META-INF/neoforge.mods.toml` for NeoForge. Each descriptor declares `${gametestModuleName}` and depends on the production `${id}`. The essential Forge form is:

```toml
modLoader = "lowcodefml"
loaderVersion = "*"
license = "YOUR_PROJECT_LICENSE"

[[mods]]
modId = "${gametestModuleName}"
version = "${version}"
displayName = "${name} Game Tests"

[[dependencies.${gametestModuleName}]]
modId = "${id}"
mandatory = true
versionRange = "*"
ordering = "AFTER"
side = "BOTH"
```

Replace `YOUR_PROJECT_LICENSE` with the production project's actual license. For NeoForge, use the same module declaration in `neoforge.mods.toml` and replace `mandatory = true` with `type = "required"`. Stonecraft adds this test module only to the GameTest runs and preserves the project's other Loom mod groups.

Stonecraft provides build and run integration; it does not make loader-specific GameTest registration APIs portable. Fabric discovers the class named by the `fabric-gametest` entrypoint. On legacy Forge and NeoForge, annotated tests belong to `${gametestModuleName}`; their structure namespace can still be the production `${id}` and must be specified with the API supported by that loader version. On 1.21.5 and later, data-driven test instances remain test-only resources while their test-function registrar joins the production mod so it can subscribe to that mod's event bus. Stonecraft enables both namespaces in Forge-like GameTest runs. Keep these API differences in Stonecutter conditional source. The generated project's `ExampleGameTests` class demonstrates the modern registration pattern.

## Test-only dependencies

Use `gametestImplementation` for ordinary test-only libraries. On mapped Minecraft versions, use Loom's `modGametestImplementation` configuration for mod dependencies that must be remapped:

```kotlin title="build.gradle.kts"
dependencies {
    gametestImplementation("com.example:test-support:1.0")
    modGametestImplementation("com.example:test-mod:1.0")
}
```

Minecraft 26.1 and later is unobfuscated, so it has no Loom remap configurations. Use `gametestImplementation` there.

## Migrating an existing project

Move GameTest classes from `src/main/java` to `src/gametest/java`. This is their physical source location on every version: Stonecraft assigns legacy annotated classes to the GameTest module at runtime, and assigns 1.21.5+ registrar classes to the production mod's event-bus group during the GameTest run. Move test structures, test-instance JSON and other test-only data from `src/main/resources` to `src/gametest/resources`; those resources remain owned by the GameTest module on every version. Put loader descriptors in `src/gametestModule/resources`.

For Fabric, move the `fabric-gametest` entrypoint out of the production `fabric.mod.json` and into the test-only descriptor shown above. Add the Forge and NeoForge test descriptors when those loaders are present. The existing task names remain unchanged. [`gametestEntrypointCleanup`](01-modsettings/gametestEntrypointCleanup.md) remains available only for projects that have not yet moved the Fabric entrypoint.
