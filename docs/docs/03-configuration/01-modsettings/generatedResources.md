# generatedResources

| Key                | Type      | Default                                                   |
|--------------------|-----------|-----------------------------------------------------------|
| generatedResources | Directory | project.layout.projectDirectory.dir("src/main/generated") |


The `generatedResources` setting controls the base directory used for data generation. Stonecraft
applies it independently to every Minecraft-version and loader pair.

For NeoForge projects targeting Minecraft 1.21.4 or later, Stonecraft writes client output to
`<generatedResources>/client` and server output to `<generatedResources>/server`. Each directory is
registered directly as a resource root, so either run can clean stale files without deleting output
owned by the other.

Fabric, Forge, and NeoForge projects targeting versions before Minecraft 1.21.4 write directly to
`generatedResources`.

```kotlin title="build.gradle.kts"
modSettings {
    generatedResources = project.layout.projectDirectory.dir("src/main/generated")
}
```

## Overriding

Set `generatedResources` to a different project directory:

```kotlin title="build.gradle.kts"
modSettings {
    generatedResources = project.layout.projectDirectory.dir("generated")
}
```

For NeoForge 1.21.4 or later, this produces `generated/client` and `generated/server`; other
configurations write directly to `generated`.
