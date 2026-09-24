# Variable Substitution

Stonecraft helps with variable substitution in your resources.
It hooks into the ProcessResources task and replaces the variables in your resources with the values you set in your `build.gradle[.kts] file`.
Check the [configuration](/docs/configuration/modsettings/variableReplacements) for more information on how to set this up.

## Example 

This is an example of a `fabric.mod.json` file that uses variable substitution.

```json title="fabric.mod.js"
{
  "schemaVersion": 1,
  "id": "${id}",
  "version": "${version}",

  "name": "${name}",
  "description": "${description}",
  "authors": [],
  "contact": {},

  "environment": "*",
  "entrypoints": {
    "main": [
      "${group}.${id}.ExampleMod"
    ]
  },
  "depends": {
    "fabricloader": "*",
    "fabric-api": ">=${fabricVersion}",
    "minecraft": ">=${minecraftVersion}"
  }
}

```

## Files

All `json`, `toml`, and `mcmeta` files in your resources are automatically included for variable substitution.

### Language files

Language files within the `assets` folder are the only exceptions because Minecraft translations use a variable substitution system of their own
which is not compatible with gradle's variable substitution.

## Default Variables

These are the variables that are available for you to use in your resources without any additional settings.

### `id`

The mod ID of your mod. This is the same as the `mod.id` property in your `settings.gradle[.kts]` file.

### `name`

The name of your mod. This is the same as the `mod.name` property in your `settings.gradle[.kts]` file.

### `group`

The group of your mod. This is the same as the `mod.group` property in your `settings.gradle[.kts]` file.

### `description`

The description of your mod. This is the same as the `mod.description` property in your `settings.gradle[.kts]` file.

### `version`

The version of your mod. This is the same as the `mod.version` property in your `settings.gradle[.kts]` file.

### `minecraftVersion`

The Minecraft version of your mod. This is the resolved Minecraft version for the gradle task you are running.

### `packVersion`

The resource pack format belonging to the Minecraft version you are using. Stonecraft supplies this value from its
bundled Minecraft metadata.

Stonecraft looks up pack formats as follows:

- A Minecraft ID present in the bundled metadata uses its exact datapack and resource pack formats.
- An ID absent from the bundled metadata is assumed to be newer. It uses both formats from the single bundled entry
  with the latest `releaseTime`.

This fallback is based on the newest metadata bundled with your installed Stonecraft version. It does not confirm
that the unknown Minecraft ID actually uses those formats.

The fallback follows the normal substitution and metadata rules. Unless you override `packVersion` in
`variableReplacements`, a `${packVersion}` placeholder receives the fallback resource pack format. A literal value
remains unchanged. For Forge, a source `pack.mcmeta` takes precedence over Stonecraft's generated file; placeholders
within that source file are still substituted.

To use a specific resource pack format instead of Stonecraft's value, set the replacement in `build.gradle.kts`:

```kotlin
modSettings {
    variableReplacements = mapOf("packVersion" to 98)
}
```

### `fabricVersion`

The Fabric API version of your mod. This is the same as the `fabric_version` 
property in your `versions/dependencies/[MinecraftVersion].properties` file.

### `forgeVersion`

The Forge version of your mod. This is the same as the `forge_version`
property in your `versions/dependencies/[MinecraftVersion].properties` file.

### `neoforgeVersion`

The NeoForge version of your mod. This is the same as the `neoforge_version`
property in your `versions/dependencies/[MinecraftVersion].properties` file.
