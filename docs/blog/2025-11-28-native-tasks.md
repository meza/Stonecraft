---
title: Stonecutter 0.7 and the Future of chiseled* Tasks
description: Why Stonecraft encourages the native Gradle tasks moving forward
slug: native-tasks-deprecation
authors: meza
---

Stonecutter 0.7 changed the build model substantially: chiseled tasks were removed upstream, and the plugin now wires the regular Gradle tasks (`build`, `publishMods`, etc.) across every configured node. Running `./gradlew build` already processes every loader/version pair. There is no special task required anymore. If you need more detail on Stonecutter's side of the migration, check the [official update notes](https://stonecutter.kikugie.dev/blog/wall/0.7-update#chiseled-tasks).

Stonecraft has always offered `chiseled*` wrappers in the `modsAll` group (`chiseledBuild`, `chiseledPublishMods`, and friends) so CI pipelines had a single entry point. We kept those wrappers in place when migrating to Stonecutter 0.7 so existing projects could update without breaking build scripts.

## Compatibility today

- The native Gradle tasks now provide the same “fan out across all Stonecutter nodes” behavior as the old `chiseled*` wrappers.
- Stonecraft still registers the `modsAll` group and the full suite of `chiseled*` wrappers for backwards compatibility. Nothing is broken today, and there is no immediate demand to change your CI configuration.

## Looking forward

We want to encourage everyone to switch to the standard tasks. They are the API Stonecutter actively maintains, they reduce cognitive load for newcomers, and they work even if Stonecraft is not present. As more modders rely on the native tasks directly, we can eventually sunset the `modsAll` group and its wrappers entirely.

There is **no deprecation timeline yet**. We also understand some teams prefer the explicit wrapper tasks, so we’ll keep them around until the community is comfortable with the change. If you have strong feelings or use cases that still need the wrappers, please let us know on Discord so we can incorporate that feedback before setting a date. 

In the meantime, consider updating your documentation and CI pipelines to run `./gradlew build` or `./gradlew publishMods` directly. Doing so makes the transition painless when we finally remove the compatibility layer.
