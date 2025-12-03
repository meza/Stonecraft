# Dealing with pack-format updates

Occasionally when Mojang releases a new version of Minecraft, the `update-pack-versions.yml` workflow creates pull requests to update the pack format version in Stonecraft's configuration.
When this happens, do not comment on it, just ignore it as it's part of an automated pipeline.

If you create an unresolved pull request conversation, that blocks the automation and prevents future updates from being applied.

## How to spot these PRs

The conventional commit message prefix for these PRs is always exactly: `fix(pack-format):`

