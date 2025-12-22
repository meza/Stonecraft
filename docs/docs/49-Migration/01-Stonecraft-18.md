---
sidebar_position: 1
title: Stonecraft 1.7.x to 1.8.x
description: Steps to upgrade Stonecraft from 1.7.x to 1.8.x, including Stonecutter 0.7.x adoption.
---

# Stonecraft 1.7.x to 1.8.x Migration Guide

:::warning
Stonecraft `1.7.x` is end-of-life and receives no further updates.
:::

## Dependencies

Make sure your `settings.gradle[.kts]` uses the correct Stonecraft and Stonecutter versions:

```kotlin title="settings.gradle.kts"
plugins {
    id("gg.meza.stonecraft") version "1.8.+"
    id("dev.kikugie.stonecutter") version "0.7.+"
}
```

## New version string

Stonecutter 0.7 deprecated the `vers()` function that we used in the `settings.gradle.kts` file to declare the project versions.

Replace all instances of `vers(...)` with `version(...)` in your `settings.gradle[.kts]` file. 
The parameters are the same, so this should be a straightforward find-and-replace operation.

Change:

```kotlin title="settings.gradle.kts"
fun mc(version: String, vararg loaders: String) {
    for (it in loaders) vers("$version-$it", version)
}
```

To:

```kotlin title="settings.gradle.kts"
fun mc(version: String, vararg loaders: String) {
    for (it in loaders) version("$version-$it", version)
}
```

:::warning
`version()` and `versions()` might look the same, but they do VERY different things.
Make sure to use the singular `version()` function as shown above. 

_Unless you want to debug for hours like me... (meza)_
:::


## Stop using the chiseled* tasks

Stonecutter 0.7 introduced native publishing tasks that replace the chiseled* tasks.

This means that the old 

- `chiseledBuild` is now just `build`
- `chiseledBuildAndCollect` is just `buildAndCollect`
- `chiseledPublishMods` is now `publishMods`

Basically anything that is currently within the `modsAll` group in gradle is redundant and you can use the native tasks in the root project.

The `mod` group is still there for convenience as it will always point to the current Stonecutter version, but for tasks that work with all versions, use the native tasks.

:::note
Stonecraft 1.8+ still registers the `chiseled*` task aliases for backward compatibility, but they are deprecated and will be removed in a future release.
:::

## Anything else?

There are a plethora of new changes in Stonecutter 0.7.x that you can read about in the [Stonecutter 0.7 Release Notes](https://stonecutter.kikugie.dev/blog/changes/0.7)
