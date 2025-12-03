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

The pack version of your mod. This is the resource pack version belonging to the Minecraft version you are using.
It's supplied by Stonecraft.

### `fabricVersion`

The Fabric API version of your mod. This is the same as the `fabric_version` 
property in your `versions/dependencies/[MinecraftVersion].properties` file.

### `forgeVersion`

The Forge version of your mod. This is the same as the `forge_version`
property in your `versions/dependencies/[MinecraftVersion].properties` file.

### `neoforgeVersion`

The NeoForge version of your mod. This is the same as the `neoforge_version`
property in your `versions/dependencies/[MinecraftVersion].properties` file.
