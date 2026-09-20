# Stonecraft project generator

Use the [Stonecraft project generator](/generator) to create a complete mod workspace for Fabric
and NeoForge across the supported Minecraft versions.

The generator runs entirely in your browser. It downloads the project template, applies the details
you enter, and gives you a ZIP without uploading those details or contacting a repository host.

Your project details and complete feature selection are reflected in the page URL as you edit them.
Copy that URL to share a setup; opening it restores the same form state. Project details still remain
in the browser, but anyone who receives the URL can read the values encoded in it.

The generated workspace targets Minecraft 26.1, 26.2, and 26.3 with both Fabric and NeoForge.

Four project-detail fields are required. The repository URL is optional. The Mod ID is derived from
the Mod name as you type and remains editable:

| Field | What to enter | Where it is used |
| --- | --- | --- |
| Mod name | The display name without quotes, dollar signs, or backslashes | Mod metadata and the generated entrypoint class |
| Mod ID | 2-64 lowercase letters, numbers, or underscores, starting with a letter | Mod metadata, resource namespaces, and the access widener |
| Group | A lowercase, dot-separated Java package such as `dev.example` | The generated Java package |
| Author | The person, team, or organisation responsible for the mod, up to 100 characters and without quotes, dollar signs, or backslashes | Author metadata and MIT copyright holder |
| Repository URL | An optional absolute URL for the source repository, up to 2,048 characters; encode quotes, dollar signs, and backslashes | Loader metadata and the generated README when provided |

The generated metadata starts with `A Minecraft mod called <Mod name>.` Edit `mod.description` in
the downloaded project's `gradle.properties` when you are ready to replace it.

The feature selector controls complete project capabilities. All five are included by default:

| Feature | Included files and configuration |
| --- | --- |
| Data generation | Example providers, loader metadata, the DataGen script, CI commands, and generated-project guidance |
| GameTests | Cross-loader test source and resources, CI commands, and generated-project guidance |
| Mod publishing | Modrinth and CurseForge Gradle configuration, release integration, and generated-project guidance |
| Automated releases | Semantic-release configuration, release script, and release-aware GitHub Actions workflow |
| Renovate dependency updates | Root-level `renovate.json` configuration and generated-project guidance |

Publishing and automated releases are independent. A project can retain manual Modrinth and
CurseForge publishing without semantic-release, or use semantic-release for GitHub releases without
publishing to either mod platform.

1. Open the [project generator](/generator).
2. Enter the four required project details and, optionally, a repository URL.
3. Keep or clear each project feature.
4. Select **Generate project** to download the ZIP.
5. Unzip it into a new directory.
6. Follow the generated README for the capabilities you selected.

The generated README contains only the commands and configuration relevant to the selected
capabilities.
