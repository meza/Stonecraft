# generatedResources

| Key                | Type      | Default                                                   |
|--------------------|-----------|-----------------------------------------------------------|
| generatedResources | Directory | project.layout.projectDirectory.dir("src/main/generated") |


The `generatedResources` setting is useful when you are using data generation.
Stonecraft configures the data generation tasks to output the resources to this directory and then adds it to the resources source set
for each module.

This is done for every `mcVersion-loader` pair.

```kotlin title="build.gradle.kts"
modSettings {
    generatedResources = project.layout.projectDirectory.dir("src/main/generated")
}
```
