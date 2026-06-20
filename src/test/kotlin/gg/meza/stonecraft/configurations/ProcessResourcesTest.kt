package gg.meza.stonecraft.configurations

import com.google.gson.GsonBuilder
import gg.meza.stonecraft.IntegrationTest
import org.gradle.api.file.FileSystemLocation
import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.zip.ZipFile

@Suppress("DEPRECATION")
class ProcessResourcesTest : IntegrationTest {

    private lateinit var gradleTest: IntegrationTest.TestBuilder
    private lateinit var result: BuildResult

    @BeforeEach
    fun setUp() {
        if (::result.isInitialized) {
            return
        }

        gradleTest = gradleTest().buildScript(
            """
modSettings {
    variableReplacements = mapOf(
        "custom1" to "customValue1",
        "custom2" to "customValue2",
        "custom3" to "customValue3"
    )
}            
            """.trimIndent()
        )
            .setStonecutterVersion("1.20.2", "fabric", "forge")
            .setStonecutterVersion("1.21", "fabric", "forge", "neoforge")
            .withProperties(
                mapOf(
                    "mod.id" to "examplemod",
                    "mod.name" to "Test Example Mod",
                    "mod.description" to "This is a test example mod description",
                    "mod.group" to "net.example",
                    "mod.version" to "1.0",
                    "org.gradle.caching" to "false"
                )
            )

        gradleTest.run("clean", cacheTask = false)
        result = gradleTest.run("buildAndCollect")
        gradleTest.assertNoGradleFailures(result)
    }

    @Test
    fun `all relevant files made it to their correct places`() {
        val fabric1202Paths = getPathsFor(
            "1.20.2",
            "fabric",
            listOf(
                "fabric.mod.json",
                "processResourcesTest/test.json",
                "processResourcesTest/test.toml",
                "data/examplemod/advancements/recipes/empty.json",
                "data/examplemod/loot_tables/empty.json",
                "data/examplemod/recipes/empty.json",
                "data/examplemod/structures/empty.nbt",
            )
        )

        val fabric1202PathsDenied = getPathsFor(
            "1.20.2",
            "fabric",
            listOf(
                "pack.mcmeta",
                "META-INF/mods.toml",
                "META-INF/neoforge.mods.toml",
            )
        )

        val forge1202Paths = getPathsFor(
            "1.20.2",
            "forge",
            listOf(
                "pack.mcmeta",
                "META-INF/mods.toml",
                "processResourcesTest/test.json",
                "processResourcesTest/test.toml",
                "data/examplemod/advancements/recipes/empty.json",
                "data/examplemod/loot_tables/empty.json",
                "data/examplemod/recipes/empty.json",
                "data/examplemod/structures/empty.nbt",
            )
        )

        val forge1202PathsDenied = getPathsFor(
            "1.20.2",
            "forge",
            listOf(
                "fabric.mod.json",
                "META-INF/neoforge.mods.toml",
            )
        )

        val fabric121Paths = getPathsFor(
            "1.21",
            "fabric",
            listOf(
                "fabric.mod.json",
                "processResourcesTest/test.json",
                "processResourcesTest/test.toml",
                "data/examplemod/advancement/recipe/empty.json",
                "data/examplemod/loot_table/empty.json",
                "data/examplemod/recipe/empty.json",
                "data/examplemod/structure/empty.nbt",
            )
        )

        val fabric121PathsDenied = getPathsFor(
            "1.21",
            "fabric",
            listOf(
                "pack.mcmeta",
                "META-INF/mods.toml",
                "META-INF/neoforge.mods.toml",
            )
        )

        val forge121Paths = getPathsFor(
            "1.21",
            "forge",
            listOf(
                "pack.mcmeta",
                "META-INF/mods.toml",
                "processResourcesTest/test.json",
                "processResourcesTest/test.toml",
                "data/examplemod/advancement/recipe/empty.json",
                "data/examplemod/loot_table/empty.json",
                "data/examplemod/recipe/empty.json",
                "data/examplemod/structure/empty.nbt",
            )
        )

        val forge121PathsDenied = getPathsFor(
            "1.21",
            "forge",
            listOf(
                "fabric.mod.json",
                "META-INF/neoforge.mods.toml",
            )
        )

        val neoforge121Paths = getPathsFor(
            "1.21",
            "neoforge",
            listOf(
                "META-INF/neoforge.mods.toml",
                "processResourcesTest/test.json",
                "processResourcesTest/test.toml",
                "data/examplemod/advancement/recipe/empty.json",
                "data/examplemod/loot_table/empty.json",
                "data/examplemod/recipe/empty.json",
                "data/examplemod/structure/empty.nbt",
            )
        )

        val neoforge121PathsDenied = getPathsFor(
            "1.21",
            "neoforge",
            listOf(
                "pack.mcmeta",
                "META-INF/mods.toml",
                "fabric.mod.json",
            )
        )

        val pathsToTest = listOf(fabric1202Paths, forge1202Paths, fabric121Paths, forge121Paths, neoforge121Paths)
        val pathsDenied = listOf(
            fabric1202PathsDenied,
            forge1202PathsDenied,
            fabric121PathsDenied,
            forge121PathsDenied,
            neoforge121PathsDenied
        )

        pathsToTest.forEach { paths ->
            paths.forEach { path ->
                val file = path.asFile
                assertTrue(file.exists(), "File ${file.absolutePath} does not exist")
            }
        }

        pathsDenied.forEach { paths ->
            paths.forEach { path ->
                val file = path.asFile
                assertFalse(file.exists(), "File ${file.absolutePath} exists")
            }
        }
    }

    @Test
    fun `basic variable substitution works`() {
        val fabric1202Paths = getPathsFor(
            "1.20.2",
            "fabric",
            listOf(
                "processResourcesTest/test.json",
                "processResourcesTest/test.toml"
            )
        )

        val forge1202Paths = getPathsFor(
            "1.20.2",
            "forge",
            listOf(
                "processResourcesTest/test.json",
                "processResourcesTest/test.toml"
            )
        )

        val fabric121Paths = getPathsFor(
            "1.21",
            "fabric",
            listOf(
                "processResourcesTest/test.json",
                "processResourcesTest/test.toml"
            )
        )

        val forge121Paths = getPathsFor(
            "1.21",
            "forge",
            listOf(
                "processResourcesTest/test.json",
                "processResourcesTest/test.toml"
            )
        )
        val neoforge121Paths = getPathsFor(
            "1.21",
            "neoforge",
            listOf(
                "processResourcesTest/test.json",
                "processResourcesTest/test.toml"
            )
        )

        listOf(fabric1202Paths, forge1202Paths, fabric121Paths, forge121Paths, neoforge121Paths).forEach { paths ->
            paths.forEach { path ->
                val file = path.asFile
                val content = file.readText()

                if (file.extension == "toml") {
                    assertTrue(
                        content.contains("id=\"examplemod\""),
                        "File ${file.absolutePath} does not contain id=examplemod"
                    )
                    assertTrue(
                        content.contains("version=\"1.0\""),
                        "File ${file.absolutePath} does not contain version=1.0"
                    )
                    assertTrue(
                        content.contains("name=\"Test Example Mod\""),
                        "File ${file.absolutePath} does not contain name=Test Example Mod"
                    )
                    assertTrue(
                        content.contains("description=\"This is a test example mod description\""),
                        "File ${file.absolutePath} does not contain description=This is a test example mod description"
                    )
                    assertTrue(
                        content.contains("group=\"net.example\""),
                        "File ${file.absolutePath} does not contain group=net.example"
                    )

                    assertTrue(
                        content.contains("custom1=\"customValue1\""),
                        "File ${file.absolutePath} does not contain custom1=customValue1"
                    )
                    assertTrue(
                        content.contains("custom2=\"customValue2\""),
                        "File ${file.absolutePath} does not contain custom2=customValue2"
                    )
                    assertTrue(
                        content.contains("custom3=\"customValue3\""),
                        "File ${file.absolutePath} does not contain custom3=customValue3"
                    )
                }

                if (file.extension == "json") {
                    val gson = GsonBuilder().create()
                    val json = gson.fromJson(content, Map::class.java)

                    assertEquals(
                        "examplemod",
                        json["id"],
                        "File ${file.absolutePath} does not contain id=examplemod"
                    )
                    assertEquals("1.0", json["version"], "File ${file.absolutePath} does not contain version=1.0")
                    assertEquals(
                        "Test Example Mod",
                        json["name"],
                        "File ${file.absolutePath} does not contain name=Test Example Mod"
                    )
                    assertEquals(
                        "This is a test example mod description",
                        json["description"],
                        "File ${file.absolutePath} does not contain description=This is a test example mod description"
                    )
                    assertEquals(
                        "net.example",
                        json["group"],
                        "File ${file.absolutePath} does not contain group=net.example"
                    )
                    assertEquals(
                        "customValue1",
                        json["custom1"],
                        "File ${file.absolutePath} does not contain custom1=customValue1"
                    )
                    assertEquals(
                        "customValue2",
                        json["custom2"],
                        "File ${file.absolutePath} does not contain custom2=customValue2"
                    )
                    assertEquals(
                        "customValue3",
                        json["custom3"],
                        "File ${file.absolutePath} does not contain custom3=customValue3"
                    )
                }
            }
        }
    }

    @Test
    fun `build and collect removes fabric gametest entrypoint from fabric mod json`() {
        val fabricModJson = getPathsFor("1.21", "fabric", listOf("fabric.mod.json")).first().asFile
        val entrypoints = readFabricEntrypoints(fabricModJson.readText())

        assertFalse(
            entrypoints.containsKey("fabric-gametest"),
            "fabric-gametest entrypoint should be removed from ${fabricModJson.absolutePath} for build targets"
        )
    }

    @Test
    fun `gametest target keeps fabric gametest entrypoint in fabric mod json`() {
        assertGametestTargetKeepsFabricGametestEntrypoint("runGameTestServer")
    }

    @Test
    fun `gametest client target keeps fabric gametest entrypoint in fabric mod json`() {
        assertGametestTargetKeepsFabricGametestEntrypoint("runGameTestClient")
    }

    @Test
    fun `gametest target restores fabric gametest entrypoint after build target stripped it`() {
        val gametest = gametestResourceProject()

        gametest.run("clean", cacheTask = false)
        gametest.run("buildAndCollect", cacheTask = false)
        gametest.run("runGameTestServer", cacheTask = false)

        val fabricModJson = gametest.project().layout.projectDirectory
            .file("versions/1.21.4-fabric/build/resources/main/fabric.mod.json")
            .asFile
        val entrypoints = readFabricEntrypoints(fabricModJson.readText())

        assertTrue(
            entrypoints.containsKey("fabric-gametest"),
            "fabric-gametest entrypoint should be restored in ${fabricModJson.absolutePath} when a gametest target runs after a build target"
        )
    }

    @Test
    fun `build target keeps fabric gametest entrypoint when gametest entrypoint cleanup is disabled`() {
        val gametest = gametestResourceProject(
            """
    gametestEntrypointCleanup = false
            """.trimIndent()
        )

        gametest.run("clean", cacheTask = false)
        gametest.run("buildAndCollect", cacheTask = false)

        val fabricModJson = gametest.project().layout.projectDirectory
            .file("versions/1.21.4-fabric/build/resources/main/fabric.mod.json")
            .asFile
        val entrypoints = readFabricEntrypoints(fabricModJson.readText())

        assertTrue(
            entrypoints.containsKey("fabric-gametest"),
            "fabric-gametest entrypoint should remain in ${fabricModJson.absolutePath} when gametestEntrypointCleanup is false"
        )

        val jar = gametest.project().layout.projectDirectory
            .file("versions/1.21.4-fabric/build/libs/examplemod-fabric-1.0+mc1.21.4.jar")
            .asFile
        val artifactEntrypoints = readFabricEntrypointsFromJar(jar)

        assertTrue(
            artifactEntrypoints.containsKey("fabric-gametest"),
            "fabric-gametest entrypoint should remain in ${jar.absolutePath} when gametestEntrypointCleanup is false"
        )
    }

    @Test
    fun `mixed gametest build and publish invocation strips fabric gametest entrypoint from artifact`() {
        val gametest = gametestResourceProject()

        gametest.run("clean", cacheTask = false)
        gametest.run(
            listOf("chiseledTest", "chiseledGameTest", "buildAndCollect", "chiseledPublishMods"),
            cacheTask = false
        )

        val fabricModJson = gametest.project().layout.projectDirectory
            .file("versions/1.21.4-fabric/build/resources/main/fabric.mod.json")
            .asFile
        val resourceEntrypoints = readFabricEntrypoints(fabricModJson.readText())
        assertTrue(
            resourceEntrypoints.containsKey("fabric-gametest"),
            "fabric-gametest entrypoint should remain in ${fabricModJson.absolutePath} for the gametest task in a mixed invocation"
        )

        val jar = gametest.project().layout.projectDirectory
            .file("versions/1.21.4-fabric/build/libs/examplemod-fabric-1.0+mc1.21.4.jar")
            .asFile
        val artifactEntrypoints = readFabricEntrypointsFromJar(jar)

        assertFalse(
            artifactEntrypoints.containsKey("fabric-gametest"),
            "fabric-gametest entrypoint should be removed from ${jar.absolutePath} for build and publish artifacts"
        )
    }

    private fun assertGametestTargetKeepsFabricGametestEntrypoint(taskName: String) {
        val gametest = gametestResourceProject()

        gametest.run("clean", cacheTask = false)
        gametest.run(taskName)

        val fabricModJson = gametest.project().layout.projectDirectory
            .file("versions/1.21.4-fabric/build/resources/main/fabric.mod.json")
            .asFile
        val entrypoints = readFabricEntrypoints(fabricModJson.readText())

        assertTrue(
            entrypoints.containsKey("fabric-gametest"),
            "fabric-gametest entrypoint should remain in ${fabricModJson.absolutePath} for $taskName"
        )
    }

    private fun gametestResourceProject(modSettings: String = ""): IntegrationTest.TestBuilder = gradleTest().buildScript(
        """
modSettings {
    variableReplacements = mapOf(
        "custom1" to "customValue1",
        "custom2" to "customValue2",
        "custom3" to "customValue3"
    )
$modSettings
}

tasks.withType<org.gradle.api.tasks.JavaExec>().configureEach {
    onlyIf { false }
}
        """.trimIndent()
    )
        .setStonecutterVersion("1.21.4", "fabric")
        .withProperties(
            mapOf(
                "mod.id" to "examplemod",
                "mod.name" to "Test Example Mod",
                "mod.description" to "This is a test example mod description",
                "mod.group" to "net.example",
                "mod.version" to "1.0",
                "org.gradle.caching" to "false"
            )
        )

    @Test
    fun `versioned replacements work as expected`() {
        val fabric1202Path = getPathsFor("1.20.2", "fabric", listOf("processResourcesTest/test.toml")).first()
        val forge1202Path = getPathsFor("1.20.2", "forge", listOf("processResourcesTest/test.toml")).first()
        val fabric121Path = getPathsFor("1.21", "fabric", listOf("processResourcesTest/test.toml")).first()
        val forge121Path = getPathsFor("1.21", "forge", listOf("processResourcesTest/test.toml")).first()
        val neoforge121Path = getPathsFor("1.21", "neoforge", listOf("processResourcesTest/test.toml")).first()

        assertTrue(fabric1202Path.asFile.readText().contains("fabricVersion=\"0.89.3+1.20.2\""))
        val forgeToml = forge1202Path.asFile.readText()
        assertTrue(forgeToml.contains("fabricVersion=\"0.89.3+1.20.2\""))
        assertTrue(forgeToml.contains("packVersion=\"18\""))

        assertTrue(fabric121Path.asFile.readText().contains("fabricVersion=\"0.102.0+1.21\""))
        assertTrue(forge121Path.asFile.readText().contains("fabricVersion=\"0.102.0+1.21\""))
        val neoToml = neoforge121Path.asFile.readText()
        assertTrue(neoToml.contains("fabricVersion=\"0.102.0+1.21\""))
        assertTrue(neoToml.contains("packVersion=\"34\""))
    }

    @Test
    fun `language files are excluded from the variable replacement`() {
        val fabric1202Path = getPathsFor("1.20.2", "fabric", listOf("assets/examplemod/lang/en_us.json")).first()
        val forge1202Path = getPathsFor("1.20.2", "forge", listOf("assets/examplemod/lang/en_us.json")).first()
        val fabric121Path = getPathsFor("1.21", "fabric", listOf("assets/examplemod/lang/en_us.json")).first()
        val forge121Path = getPathsFor("1.21", "forge", listOf("assets/examplemod/lang/en_us.json")).first()
        val neoforge121Path = getPathsFor("1.21", "neoforge", listOf("assets/examplemod/lang/en_us.json")).first()

        val files = listOf(
            fabric1202Path,
            forge1202Path,
            fabric121Path,
            forge121Path,
            neoforge121Path
        )

        files.forEach { path ->
            assertTrue(path.asFile.readText().contains("This is a line with a variable: %1\$s"))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun readFabricEntrypoints(content: String): Map<String, Any> {
        val gson = GsonBuilder().create()
        val json = gson.fromJson(content, Map::class.java)
        return json["entrypoints"] as Map<String, Any>
    }

    private fun readFabricEntrypointsFromJar(jar: java.io.File): Map<String, Any> {
        ZipFile(jar).use { zip ->
            val entry = zip.getEntry("fabric.mod.json")
            val content = zip.getInputStream(entry).bufferedReader().use { it.readText() }
            return readFabricEntrypoints(content)
        }
    }

    private fun getPathsFor(version: String, loader: String, files: List<String>): MutableList<FileSystemLocation> {
        var project = gradleTest.project()
        val basePath = project.layout.projectDirectory.dir("versions/$version-$loader/build/resources/main")
        val paths = mutableListOf<FileSystemLocation>()
        files.forEach { file ->
            paths.add(basePath.file(file))
        }
        return paths
    }
}
