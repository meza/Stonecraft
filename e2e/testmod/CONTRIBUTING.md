# Contributing

THIS FILE IS ONLY VALID FOR THE FOLDER THAT CONTAINS THIS FILE AND BELOW.

The project itself is part of Stonecraft. This project is a testbed for Stonecraft.

Stonecaft influences how mods are made, built and run. We can do a lot with unit tests in Stonecraft, but the only way to truly test that the mod works with all the loaders and Minecraft versions is to have a real mod that is built and run with Stonecraft. This project is that mod. It's focus is to ensure that Stonecraft projects can be built, tested and run without issues, and to be a place to test new features in Stonecraft itself.

Tests here are expensive because they involve building and running the mod, which will also start actual Minecraft servers and clients. So we use this as final e2e testing and to test that Stonecraft configured the modloaders correctly.

:::warning
You must use the `--refresh-dependencies ` in all the gradle commands below WHEN Stonecraft itself is changed. That is EXTREMELY important. We're using this project to test the active, local dev version of Stonecraft, so it must be assumed to have changed since the last build. If you don't include that flag, you might be building and testing against an old version of Stonecraft, which can lead to confusing test failures and wasted time.
:::

This project uses [Stonecraft](https://stonecraft.meza.gg) as the main build system (the parent of this project), which uses [Stonecutter](https://stonecutter.kikugie.dev/wiki/) under the hood. This means that traditional gradle understanding might not be enough.

## COMMENTS ARE SPECIAL

We're using [Stonecutter](https://stonecutter.kikugie.dev/wiki/) to manage multiple Minecraft versions and loaders.

Stonecutter enhances the coding process by being a preprocessor for the code. The preprocessor is managed via comments.

DO NOT ASSUME THAT COMMENTED OUT CODE IS DEAD CODE.

It's more likely to be a different Minecraft version/loader path managed by Stonecutter.

## Working on specific Minecraft version/loader

We're using [Stonecutter](https://stonecutter.kikugie.dev/wiki/) to manage multiple Minecraft versions and loaders.

### Switching between minecraft/loader permutations

Gradle has a "Set active project to <version>-<loader>" tasks, those are the ones to use.

The versions are defined in the `settings.gradle.kts` file.

### Running tasks against the active version

- `./gradlew buildActive` - build just the current active version
- `./gradlew testActiveServer` - run the current active version's server tests

### Having Stonecutter refresh the code based on the comment conditions

- `./gradlew "Refresh active project"`

## Verifying Changes

### Quick Check

To make sure that the project tests and builds correctly:

- `./gradlew test buildAndCollect`

### Full E2E Check

- `./gradlew chiseledGameTest`

### DO NOT - CRITICAL

Do not run traditional gradle compile tasks. The project uses a custom build process that includes additional steps beyond compilation. Running standard compile tasks may lead to incomplete builds and test failures.

Do NOT manage the Stonecutter guard comments manually. Use the version switching tasks to switch between versions and loaders, and use the "Refresh active project" task to refresh the code based on the comment conditions. Manually changing the guard comments can lead to inconsistencies and errors in the build process.

## Documentation

- For the project, look in the docs folder.
- For fabric, use: https://docs.fabricmc.net/develop/
- For neoforge, use: https://docs.neoforged.net/docs/gettingstarted/
- For Minecraft: use the embedded code itself
- For Stonecraft: https://stonecraft.meza.gg/
- For Stonecutter: https://stonecutter.kikugie.dev/wiki/
