# Contributing to the project

First off, thank you for considering contributing to this project. 
It's people like you that make it a reality for the community.

Below are a set of guidelines for contributing to this project. 
These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to 
this document in a pull request.

This is an open source project, and we love to receive contributions from our community — you! 
There are many ways to contribute, from writing tutorials or blog posts, improving the documentation, 
submitting bug reports and feature requests or writing code which can be incorporated into the project itself.

## Types of Contributions

You can contribute in many ways:

### Report Bugs

Report bugs at the [issue tracker][issues].

If you are reporting a bug, please include:

- Your operating system name and version.
- Any details about your local setup that might be helpful in troubleshooting.
- If you can, provide detailed steps to reproduce the bug.
- If you don't have steps to reproduce the bug, just note your observations in as much detail as you can.
  Questions to start a discussion about the issue are welcome.

### Fix Bugs

Look through the issues for bugs.
Anything tagged with "bug" is open to whoever wants to implement it.

### Implement Features

Look through the issues for features.
Anything tagged with "enhancement" and "please-help" is open to whoever wants to implement it.

Please do not combine multiple feature enhancements into a single pull request.

### Submit Feedback

The best way to send feedback is to file an issue at [issue tracker][issues].

If you are proposing a feature:

- Explain in detail how it would work.
- Keep the scope as narrow as possible, to make it easier to implement.
- Remember that this is a volunteer-driven project, and that contributions are welcome :)

## Ground Rules

### Do

* Ensure cross-platform compatibility for every change that's accepted. Windows, Mac, Debian & Ubuntu Linux.
* Use conventional commits the right way. If you are not familiar with it, please check [this](https://www.conventionalcommits.org/en/v1.0.0/) out.
* Create issues for any major changes and enhancements that you wish to make. Discuss things transparently and get community feedback.
* Be welcoming to newcomers and encourage diverse new contributors from all backgrounds.

### Don't

* Misuse the commit messages. They are responsible for determining the correct version and are taken very seriously.
* Add any form of copyrighted material to the repository.
* Use any form of offensive language or images in the repository.
* Make changes that go against the core values of the project.

## Pull Request Guidelines

* Always branch off from `main` when creating a new branch or forking.
* A Pull Request should address one and only one concern. When you have multiple changes to propose, please create multiple PRs.
* If the Pull Request adds functionality, the README.md and all documentation should be updated accordingly.
* Do NOT submit WIP (Work in Progress) pull requests. We only accept PRs that are ready for review. 
* Pull Requests must have passing CI/CD checks before they can be merged. 
* If the CI/CD checks fail, please address the issues and push additional commits to the same branch. The PR will not be reviewed until the checks pass.

## Development

This project uses Java with Gradle and [Stonecutter][stonecutter] for development.

### Setting up

1. Clone the repository.
2. Open the project in your favorite IDE.
3. Run `./gradlew build` to build the full Minecraft version-loader matrix.

If the build is successful, you are ready to start developing. 

The project uses [Stonecutter][stonecutter] for managing different versions of Minecraft within the same codebase and
[Architectury][architectury] for handling different mod loaders. It's a very delicate ecosystem that is easy to break,
so please be careful when making changes.

### COMMENTS ARE SPECIAL

We're using [Stonecutter][stonecutter] to manage multiple Minecraft versions and loaders.

Stonecutter enhances the coding process by being a preprocessor for the code. The preprocessor is managed via comments.

DO NOT ASSUME THAT COMMENTED OUT CODE IS DEAD CODE.

It's more likely to be a different Minecraft version/loader path managed by Stonecutter.

### Working on specific Minecraft version/loader

We're using [Stonecutter][stonecutter] to manage multiple Minecraft versions and loaders.

#### Switching versions

Before testing or diagnosing one Minecraft version-loader combination, switch to it with Stonecutter's generated task:

```bash
./gradlew "Set active project to <version>-<loader>"
```

The versions are defined in the `settings.gradle.kts` file.
Switching the active project rewrites the Stonecutter-controlled branches under `src/`, so always switch before editing or testing version-dependent source.

#### Running tasks against the active version

- `./gradlew buildActive` builds only the current active version-loader combination.

Use this task after switching when testing or diagnosing one target.

#### Restoring the repo

Before committing, restore to the known canonical state with:

```bash
./gradlew "Reset active project"
```

This restores the active project to the `vcsVersion` declared in `settings.gradle.kts`.
The reset state is mandatory before full-matrix verification and before handover.
Do not edit the active project in `stonecutter.gradle.kts` manually.

#### Building the full matrix

- `./gradlew build` builds the full declared Minecraft version-loader matrix.
- `./gradlew buildAndCollect` builds the full matrix and collects the jars in one place.

### Verifying Changes

#### Quick Check

To make sure that the project tests and builds correctly:

- Run `./gradlew build` for full-matrix verification.
- Run `./gradlew buildAndCollect` instead when you also need the jars collected in one place.

### Adding a new minecraft version to the project

One of the main additions to the project is adding a new Minecraft version as mojang releases them.
Every time a new version is released, we need to set new dependencies and update the mappings at the very least.
At the worst, we need to completely rewrite sections of the mod to work with the new version of Minecraft.

Fortunately [Stonecutter][stonecutter] makes this process easier by allowing us to have multiple versions of Minecraft in the same codebase.
The trade-off is that it's a bit more complex to read, but it's worth it in the long run.

To add a new version of Minecraft to the project, follow the steps in the [Minecraft Version Update Runbook](docs/minecraft-version-update-runbook.md).

## Documentation

- For the project, look in the docs folder.
- For fabric, use: https://docs.fabricmc.net/develop/
- For neoforge, use: https://docs.neoforged.net/docs/gettingstarted/
- For Minecraft: use the embedded code itself
- For Stonecraft: https://stonecraft.meza.gg/
- For Stonecutter: https://stonecutter.kikugie.dev/wiki/

---

[issues]: __STONECRAFT_REPOSITORY_URL__
[stonecutter]: https://stonecutter.kikugie.dev/wiki
[discord]: https://discord.gg/dvg3tcQCPW
[architectury]: https://docs.architectury.dev/
