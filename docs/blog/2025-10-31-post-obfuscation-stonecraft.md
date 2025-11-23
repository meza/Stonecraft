---
title: Stonecraft in a Post-Obfuscation World
description: Mojang and Fabric are removing obfuscation from Minecraft Java Edition, and Stonecraft is ready to keep your multi-loader, multi-version builds running smoothly.
slug: post-obfuscation-stonecraft
authors: meza
---

Mojang’s recent post about [removing obfuscation in Java Edition](https://www.minecraft.net/en-us/article/removing-obfuscation-in-java-edition) lands at the same time as Fabric’s companion update on [removing obfuscation from Fabric](https://fabricmc.net/2025/10/31/obfuscation.html). Both teams want modders to spend less time deciphering symbol soup and more time building. Stonecraft exists to make multi-loader, multi-version projects routine, so a structural change to how Minecraft ships its code is a story we care about deeply.

## What Mojang’s Deobfuscation Means

Starting with the first snapshot that follows the Mounts of Mayhem launch, new builds of Minecraft: Java Edition will include Mojang’s original class, field, method, parameter and local names directly inside the jars. In the meantime Mojang is publishing “experimental release” builds so toolchains can prepare. There will no longer be obfuscation map entries in version JSONs and Fabric Loader will not need to run remapping steps for those versions. Nothing about the EULA or usage guidelines changes, but logs, stack traces and debugging sessions become dramatically easier to read because the names you see in your IDE now match the names inside the production jars.

## Fabric’s Roadmap and Toolchain Shifts

Fabric has already outlined its approach. Loom 1.13 will be the on-ramp for the experimental un-obfuscated builds, with Loom 2.0 being explored as a leaner, more modular future after the dust settles. Fabric API will keep its current feature set but align naming and documentation with Mojang’s vocabulary. Intermediary will disappear when the runtime no longer needs a neutral namespace, and Yarn will stop tracking new releases while remaining available for older versions that still need remapping.

## Stonecraft’s Commitment

Stonecraft’s promise is that your existing workflow should not change just because Mojang’s binaries get clearer. The plugin will continue to behave as it does today for every currently supported version. Multi-loader modules will keep compiling, Architectury and Stonecutter integration stay intact, and all of the existing resource processing, pack metadata generation and publishing helpers remain untouched.

## Mojmap Support Incoming

We are adding Mojmap support so that new versions can benefit from the official names without forcing older branches to change course. The plan is to introduce a conditional flag in Stonecraft’s configuration (exact key still to be determined) that lets you choose between the current Yarn-powered path and Mojang’s mappings. When the flag is enabled Stonecraft will skip the intermediary plumbing for newer versions, while legacy projects can keep their Yarn coordinates exactly as-is. Templates and docs will include the switch as soon as it lands so you can adopt it at your own pace.

## Looking Ahead

The payoff is a simpler mapping story for everyone using Stonecraft: faster setup on new snapshots, fewer moving parts in Gradle builds and clearer crash reports when players send logs back. We will continue tracking the experimental drops from Mojang and Fabric, update Stonecraft as necessary and document every change on this site. If you spot a scenario we should cover, open an issue so we can keep the transition just as smooth as the old workflow.
