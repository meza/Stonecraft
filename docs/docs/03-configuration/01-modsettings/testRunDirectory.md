# test*RunDirectory

| Key                      | Description | Default                                                       |
|--------------------------|-------------|---------------------------------------------------------------|
| `testClientRunDirectory` | Directory   | [`runDirectory`](./runDirectory.md).dir("testclient/$loader") |
| `testServerRunDirectory` | Directory   | [`runDirectory`](./runDirectory.md).dir("testserver/$loader") |

These settings are used to configure the directories where the GameTest client and GameTest server will be run.

By default, they are set to the `testclient/$loader` and `testserver/$loader` directories in the [`runDirectory`](./runDirectory.md) directory.
The loader scoping is used to ensure that things run smoothly even when some of the loaders might treat the GameTests differently.

## Overriding

You can override these settings if you want to.

```kotlin title="build.gradle.kts"
loom {
    runs {
        getByName("gameTestServer") {
            runDir = "../../run-test" // <- Loom needs a String, not a Directory provider :(
        }
    }
}
```

If you want to do a more future-proof job with the directory paths, you could try this:

```kotlin title="build.gradle.kts"
loom {
    runs {
        getByName("gameTestServer") {
            val testDir = project.layout.projectDirectory.asFile.toPath().relativize(
                rootProject.layout.projectDirectory.dir("run-test").asFile.toPath()
            ).toString()

            runDir = testDir
        }
    }
}
```
