---
sidebar_position: 6
title: Stonecraft and Stonecutter Compatibility
description: Reference chart for how each Stonecraft version line maps to Stonecutter releases.
---

# Stonecraft and Stonecutter Compatibility

Stonecraft wraps Stonecutter for multi-version workflows. Because each Stonecraft release line bakes in a specific Stonecutter baseline, **the only reliable way to target a Stonecutter version is to use the matching Stonecraft line**. The table below lists the supported combinations so you know which Stonecraft build to install when you need a particular Stonecutter API surface.

:::tip Treat Stonecraft as the compatibility shim
If you override Stonecutter manually you can break task wiring, DSL keywords, or migration helpers. Pick the Stonecraft version that already embeds the Stonecutter release you need instead of mixing and matching versions.
:::

## Current Mapping

| Stonecraft version line | Supported Stonecutter version | Notes                                                               |
|-------------------------|-------------------------------|---------------------------------------------------------------------|
| `<1.3.3`                | `0.5.x`                       | Legacy line, no new features                                        |
| `1.3.3 - 1.7.x`         | `0.6.2`                       | End-of-life (frozen). Upgrade when possible.                        |
| `1.8.x`                 | `0.7.11`                      | Maintenance mode. Receives critical fixes only.                     |
| `1.9.x`                 | `0.8.x`                       | Current stable line. Stonecutter 0.8 stable + typed task accessors. |


:::warning Support policy
- `1.9.x` is the actively maintained release line.
- `1.8.x` is in maintenance mode (critical fixes only).
- `1.7.x` is end-of-life and receives no further updates.
:::

## Experimental Snapshot Channel

Stonecraft publishes snapshot builds for users who want unreleased changes early. Because this track evolves quickly, you must opt-in explicitly by pointing Gradle at the snapshots repository and declaring matching plugin versions.

1. Add the snapshots repository anywhere you resolve Gradle plugins:

```kotlin
pluginManagement {
    repositories {
        maven("https://maven.meza.gg/snapshots")
        // keep your existing repos here
    }
}
```

2. Depend on the compatible plugin versions:

```kotlin
plugins {
    id("gg.meza.stonecraft") version "1.9.+"
    id("dev.kikugie.stonecutter") version "0.8+"
}
```

Snapshot builds can include breaking changes between bumps. Track this page for updates before promoting a build to production or pinning the line in CI.

We actively track Stonecutter development so Stonecraft users can adopt the latest tooling with minimal effort. When Stonecutter moves forward, expect a corresponding Stonecraft release to keep this compatibility matrix authoritative.
