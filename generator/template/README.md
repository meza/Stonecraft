# __STONECRAFT_MOD_NAME__

__STONECRAFT_MOD_DESCRIPTION__

This project uses [Stonecraft](https://stonecraft.meza.gg) to build for __STONECRAFT_LOADERS__. It includes:

- a shared mod entrypoint across the selected loaders;
__STONECRAFT_#DATAGEN__
- example data generation for selected loaders that provide an example generator;
__STONECRAFT_/DATAGEN____STONECRAFT_#GAMETESTS__
- a cross-loader GameTest;
__STONECRAFT_/GAMETESTS__
- an access widener that Stonecraft converts for loaders that require it;
__STONECRAFT_#PUBLISHING__
- artifact publishing for Modrinth and CurseForge;
__STONECRAFT_/PUBLISHING____STONECRAFT_#AUTOMATED_RELEASES__
- semantic-release automation;
__STONECRAFT_/AUTOMATED_RELEASES____STONECRAFT_#RENOVATE__
- Renovate configuration for dependency updates.
__STONECRAFT_/RENOVATE__

__STONECRAFT_#REPOSITORY__
Source code is available at [__STONECRAFT_REPOSITORY_URL__](__STONECRAFT_REPOSITORY_URL__).
__STONECRAFT_/REPOSITORY__

## Requirements

- JDK 25
__STONECRAFT_#AUTOMATED_RELEASES__
- Git, when using semantic-release
__STONECRAFT_/AUTOMATED_RELEASES__

The Gradle wrapper downloads the required Gradle distribution automatically.

## Build and verify

Run the available commands from the project root:

```shell
./gradlew buildAndCollect
__STONECRAFT_#DATAGEN__
./gradlew runDatagen
__STONECRAFT_/DATAGEN____STONECRAFT_#GAMETESTS__
./gradlew runGameTestServer
__STONECRAFT_/GAMETESTS__
```

`buildAndCollect` builds every configured Minecraft-version and loader pair, then collects the resulting JARs under `build/libs`.
__STONECRAFT_#DATAGEN__
`runDatagen` runs data generation for every configured pair and writes into each `versions/*/src/main/generated` directory. The example advancement provider is registered for selected Fabric and NeoForge pairs.
__STONECRAFT_/DATAGEN____STONECRAFT_#GAMETESTS__
`runGameTestServer` succeeds only after the no-op GameTest passes for every pair. Failures are reported by the corresponding version-loader Gradle task.
__STONECRAFT_/GAMETESTS__

__STONECRAFT_#PUBLISHING__
## Publishing configuration

Stonecraft's `publishMods` task performs network publication only when the `DO_PUBLISH` environment variable is `true`. Otherwise it prints the publication payload as a dry run. Modrinth and CurseForge are configured independently: a platform is skipped unless every required value for it is present, and an incomplete platform does not prevent publishing to the other one. Set the variables for the platform you want to publish to in the shell that runs Gradle, and treat both token values as secrets.

```shell
./gradlew publishMods
```

| Name | Purpose |
| --- | --- |
| `DO_PUBLISH` | Enables network publication when set to `true` |
| `RELEASE_TYPE` | Selects `stable`, `beta`, or `alpha` publication metadata |
| `MODRINTH_ID` | Modrinth project ID |
| `MODRINTH_TOKEN` | Modrinth API token |
| `CURSEFORGE_ID` | CurseForge project ID |
| `CURSEFORGE_SLUG` | CurseForge project slug |
| `CURSEFORGE_TOKEN` | CurseForge API token |

For example, this PowerShell session publishes a stable release to Modrinth:

```powershell
$env:DO_PUBLISH = "true"
$env:RELEASE_TYPE = "stable"
$env:MODRINTH_ID = "your-project-id"
$env:MODRINTH_TOKEN = "your-api-token"
./gradlew publishMods
```

Use `CURSEFORGE_ID`, `CURSEFORGE_SLUG`, and `CURSEFORGE_TOKEN` instead to publish only to CurseForge, or set both complete platform groups to publish to both.

__STONECRAFT_#AUTOMATED_RELEASES__
The release workflow reads project IDs and slugs from GitHub Actions variables and tokens from GitHub Actions secrets, then supplies them as environment variables to Gradle. Configure a platform's complete variable and secret set before pushing a release-worthy commit if artifacts should be uploaded there.
__STONECRAFT_/AUTOMATED_RELEASES__

__STONECRAFT_/PUBLISHING____STONECRAFT_#AUTOMATED_RELEASES__
## Automated releases

The GitHub Actions workflow verifies pull requests, merge queues, manual runs, and pushes to `main` or `beta`. Semantic-release analyzes commits since the latest release tag and uses the highest release level requested by those commits: `fix`, `perf`, and revert commits produce patch releases; `feat` produces a minor release; and a `BREAKING CHANGE:` footer or `!` marker produces a major release. Other commit types do not produce a release unless they declare a breaking change. The first qualifying release starts at `1.0.0`. The `beta` branch produces numbered prereleases such as `1.1.0-beta.1`, increasing the prerelease number for later qualifying changes to that version.

When no release is required, the workflow runs the selected verification tasks and builds the project. When a release is required, `scripts/release.sh` applies the release version__STONECRAFT_#DATAGEN__, regenerates data__STONECRAFT_/DATAGEN____STONECRAFT_#GAMETESTS__, runs GameTests__STONECRAFT_/GAMETESTS__, and builds the artifacts__STONECRAFT_#PUBLISHING__ before invoking publishing with `DO_PUBLISH=true`__STONECRAFT_/PUBLISHING__. Semantic-release then creates the GitHub release.

__STONECRAFT_/AUTOMATED_RELEASES____STONECRAFT_#DATAGEN__
## Generated resources

Data generation writes to `src/main/generated` inside each generated Stonecutter version project. Stonecraft includes that directory in loader builds automatically.

__STONECRAFT_/DATAGEN____STONECRAFT_#GAMETESTS__
## GameTests

The loader-specific GameTest entrypoints are available to GameTest runs. Stonecraft removes them from normal production JARs.

__STONECRAFT_/GAMETESTS__
## Access widening

Add access-widener entries to `src/main/resources/__STONECRAFT_MOD_ID__.accesswidener`. Stonecraft uses it directly where supported and converts it for loaders that use access transformers.

__STONECRAFT_#RENOVATE__
## Dependency updates

The repository includes `renovate.json`. Enable Renovate through your hosting platform's integration or a self-hosted Renovate instance. See [Installing & Onboarding](https://docs.renovatebot.com/getting-started/installing-onboarding/) for the available options. Renovate then reads the committed configuration and begins opening dependency update pull requests.
__STONECRAFT_/RENOVATE__
