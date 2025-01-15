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
