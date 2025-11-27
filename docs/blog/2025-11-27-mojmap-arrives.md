---
title: Mojmap Arrives in Stonecraft
description: Stonecraft 1.7.0 adds first-class Mojmap support and keeps legacy Yarn behavior intact.
slug: mojmap-arrives
authors: meza
---

When Mojang announced that Java Edition would ship unobfuscated jars, we promised Stonecraft builds would get simpler, not harder. Today’s 1.7.0 release delivers on that: Mojang’s official Mojmap names are now supported and become the default namespace for new Stonecraft versions, while Yarn remains available for older branches that still rely on it.

<!-- truncate -->

## Mojmap by Default

Stonecraft now wires Loom’s `officialMojangMappings()` automatically whenever a version’s dependency file omits the legacy `yarn_mappings` property. That means fresh projects and future Minecraft releases inherit Mojang’s class, field, and method names without any extra configuration. You get clearer stack traces, fewer moving parts, and parity with Mojang’s own debugging experience.

## Yarn Remains Available

Changing course shouldn’t break projects that still depend on Yarn. If a version directory already defines `yarn_mappings` (and the optional NeoForge patch coordinate), Stonecraft keeps layering Yarn into Loom exactly as it did before. NeoForge still receives the Architectury patch so its Mojmap-first toolchain plays nicely with Yarn-based mods. Remove those properties when you’re ready to switch, and Stonecraft will automatically flip that version over to Mojmap.

## Migration Tips

1. **New versions:** skip `yarn_mappings` in `versions/dependencies/<version>.properties` and you’ll get Mojmap automatically.
2. **Legacy branches:** leave existing `yarn_mappings` entries in place for as long as you need Yarn. There’s no rush.
3. **Need name translations?** Fabric’s guide on [migrating between mappings](https://wiki.fabricmc.net/tutorial:migratemappings) explains the process, and [Linkie](https://linkie.shedaniel.dev/mappings?namespace=yarn&version=1.21.10&translateMode=ns&translateAs=mojang_raw) presents Yarn and Mojmap symbols side-by-side so you can map identifiers quickly.

## What’s Next

With Mojmap support in place, we’re shifting attention back to the Stonecutter migration work outlined earlier this week. Expect incremental releases as we adopt Stonecutter 0.7/0.8, continue refreshing template projects, and document any behavior changes along the way.

## Good luck!
Good luck with the great mapping migration! May your stack traces always spell things correctly, and may Mojang’s naming quirks be ever in your favor.
