# Overriding Settings

Stonecraft's role is to configure sensible defaults. It's not restrictive in any way.

## Stonecraft comes first!

If you want to be able to overwrite anything that Stonecutter sets, you need to make sure that Stonecraft is applied first.

Below is a simple gradle file that shows you how you should think of the configuration block order.
This shows you the order in which things are set within Stonecraft. Since the modSettings block comes first, anything that
follows it can overwrite the settings it sets.

If you want to see an example, check the [Stonecraft-template](https://github.com/meza/Stonecraft-template/blob/main/build.gradle.kts)

## High Level Overview

```kotlin title="build.gradle.kts"
plugins {
    id("gg.meza.stonecraft")
}

modSettings {}

repositories {}
dependencies {}

// Architectury related settings
fabricApi {}
loom {}

// Task overrides
tasks.processResources {}

// Mod publishing
publishMods {}

```

## Overriding Minecraft Version for snapshot builds

When you are building for a snapshot version of Minecraft, you can prepare your mod for the eventual release. 
It's simpler to explain this with an example.

Let's say currently Minecraft `1.21.5` is out and you're preparing your mod for `1.21.6` and the current snapshot is `1.21.6-rc1`.

### 1. Create the `1.21.6.properties` file
Create a file named `1.21.6.properties` in the `versions/dependencies` folder with the following content:

```properties
// highlight-next-line
minecraft_version=1.21.6-rc1  # <- This is the real snapshot version that the build will use
yarn_mappings=1.21.6-rc1+build.1
loader_version=0.16.14

# Fabric API
fabric_version=0.127.0+1.21.6
```

This is what you would copy from Fabric's website for the snapshot version.

### 2. Set your stonecutter version

```kotlin title="settings.gradle.kts"
  ...
  mc("1.21.6", "fabric")  // 1.21.6 doesn't technically exist yet, but you can prepare for it
  ...
```

And that's it! Stonecraft will automatically use the `1.21.6.properties` file for the snapshot build.

## Accessing the resolved Minecraft version in build scripts

When you need the "real" Minecraft version inside your build logic (for example to gate dependencies or configure Loom), use `mod.minecraftVersion`. The property returns the override from `versions/dependencies/<version>.properties` when `minecraft_version` is present and otherwise falls back to the active Stonecutter version. This mirrors the version Stonecraft passes into resource processing and dependency configuration, so consumer projects can rely on a single value regardless of snapshot overrides.

### Example: snapshot override vs canonical version

Imagine Mojang is preparing Minecraft `1.21.11`, but the latest snapshot is `1.21.11-rc3`. You still create `versions/dependencies/1.21.11.properties` with `minecraft_version=1.21.11-rc3`, and in `settings.gradle.kts` (or `stonecutter.gradle.kts`) you prepare your matrix with `versions("1.21.11-fabric", "1.21.11")`. In this setup:

- `stonecutter.current.version` evaluates to `1.21.11` because that is the canonical line configured in Stonecutter.
- `mod.minecraftVersion` evaluates to `1.21.11-rc3` because it reads the override from the properties file.

Use `mod.minecraftVersion` whenever you need to talk to Mojang artifacts (e.g., `com.mojang:minecraft` dependencies, run configurations, or ProcessResources substitution) so your build tracks the snapshot you actually ship against while Stonecutter continues organizing the versioned workspaces by their eventual release number.
