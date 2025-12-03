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

| Stonecraft version line | Supported Stonecutter version | Notes                                                                                 |
|-------------------------|-------------------------------|---------------------------------------------------------------------------------------|
| `<1.3.3`                | `0.5.x`                       | Legacy line, no new features                                                          |
| `1.3.3 - 1.7.x`         | `0.6.2`                       | Trusted and true. If you don't like change, stay here.                                |
| `1.8.x`                 | `0.7.11`                      | Embracing Stonecutter 0.7 stable features. Allows you to abandon the chiseled* tasks. See [our native tasks write-up](/blog/native-tasks-deprecation) for context. |
| `1.9.x`                 | `0.8+ (alpha)`                | Cutting-edge Stonecutter 0.8 features, expect breaking changes as upstream stabilizes |


:::warning Older Stonecraft lines are frozen
Legacy releases only receive critical security fixes. 
New Stonecutter functionality is added exclusively to newer Stonecraft branches, so upgrade when you want modern tasks and DSL helpers.
:::

We actively track Stonecutter development so Stonecraft users can adopt the latest tooling with minimal effort. When Stonecutter moves forward, expect a corresponding Stonecraft release to keep this compatibility matrix authoritative.
