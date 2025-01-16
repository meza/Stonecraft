# fabric*JunitReportLocation

| Key                               | Type        | Default                                                |
|-----------------------------------|-------------|--------------------------------------------------------|
| `fabricClientJunitReportLocation` | RegularFile | project.layout.buildDirectory.file("junit-client.xml") |
| `fabricServerJunitReportLocation` | RegularFile | project.layout.buildDirectory.file("junit-server.xml") |

These settings get configured for the **fabric** GameTest runners.
The default values are set to the build directory of the [`projects`](../index.mdx#project-vs-root-project) with the respective file names.

## Overriding

You can override these settings if you want to.

```kotlin title="build.gradle.kts"
import gg.meza.stonecraft.mod

loom {
    runs {
        if (mod.isFabric) {
            getByName("gameTestServer") {
                vmArg("-Dfabric-api.gametest.report-file=${rootProject.file("build/junit.xml")}")
            }

            getByName("gameTestClient") {
                vmArg("-Dfabric-api.gametest.report-file=${rootProject.file("build/junit.xml")}")
            }
        }
    }
}
```
