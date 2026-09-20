package gg.meza.stonecraft.testmod

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

@DisplayName("Testmod build")
class TestModBasicsTest : IntegrationTest {

    @Test
    fun `forge metadata requires an explicit loader version`() {
        val gradleTest = gradleTestMod()
        val dependencyProperties = gradleTest.project().layout.projectDirectory
            .file("versions/dependencies/1.20.4.properties")
            .asFile
        dependencyProperties.writeText(
            dependencyProperties.readLines()
                .filterNot { it.startsWith("forge_loader_version=") }
                .joinToString("\n")
        )

        val result = gradleTest.run(":1.20.4-forge:processResources", cacheTask = false)

        assertTrue(
            result.output.contains("BUILD FAILED"),
            "Expected missing Forge loader metadata to fail the build. Output:\n${result.output}"
        )
        assertTrue(
            result.output.contains("forge_loader_version"),
            "Expected the missing Forge loader property to be named. Output:\n${result.output}"
        )
    }

    @Test
    fun `testmod can build and collect jars`() {
        val gradleTest = gradleTestMod()

        val result = gradleTest.run("buildAndCollect", cacheTask = false)

        gradleTest.assertNoGradleFailures(result)

        val collectedJars = gradleTest.project()
            .layout.projectDirectory
            .dir("build/libs")
            .asFile
            .listFiles { file -> file.extension == "jar" }
            .orEmpty()
            .toList()

        assertTrue(
            collectedJars.any { file -> file.name.startsWith("stonecraft_testmod-") },
            "Expected buildAndCollect to collect testmod jars. Found: ${collectedJars.joinToString { it.name }}"
        )

        assertFabricAccessWidener(
            collectedJars.jarNamed("stonecraft_testmod-fabric-0.0-SNAPSHOT+mc1.20.4.jar"),
            "stonecraft_testmod.old.accesswidener"
        )
        assertFabricAccessWidener(
            collectedJars.jarNamed("stonecraft_testmod-fabric-0.0-SNAPSHOT+mc26.1.jar"),
            "stonecraft_testmod.accesswidener"
        )
        assertNeoForgeAccessTransformer(
            collectedJars.jarNamed("stonecraft_testmod-neoforge-0.0-SNAPSHOT+mc1.20.4.jar")
        )
        assertNeoForgeAccessTransformer(
            collectedJars.jarNamed("stonecraft_testmod-neoforge-0.0-SNAPSHOT+mc26.1.jar")
        )
        assertForgeLoaderRange(
            collectedJars.jarNamed("stonecraft_testmod-forge-0.0-SNAPSHOT+mc1.20.4.jar"),
            "49.2.7"
        )
        assertForgeLoaderRange(
            collectedJars.jarNamed("stonecraft_testmod-forge-0.0-SNAPSHOT+mc26.1.jar"),
            "62.0.9"
        )
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.MINUTES)
    fun `testmod can run chiseled gametest`() {
        val gradleTest = gradleTestMod()

        val result = gradleTest.run("chiseledGameTest", cacheTask = false)

        gradleTest.assertNoGradleFailures(result)

        val successMarkers = listOf(
            "required tests pass",
            "GAME TESTS COMPLETE"
        )

        successMarkers.forEach { marker ->
            assertTrue(
                result.output.contains(marker),
                "Expected Gradle success marker '$marker'. Output:\n${result.output}"
            )
        }
    }

    private fun assertFabricAccessWidener(jar: File, expectedAccessWidener: String) {
        ZipFile(jar).use { zip ->
            val fabricModJson = zip.readEntry("fabric.mod.json")

            assertTrue(
                fabricModJson.contains("\"accessWidener\": \"$expectedAccessWidener\""),
                "Expected ${jar.name} to declare accessWidener=$expectedAccessWidener. fabric.mod.json:\n$fabricModJson"
            )
            assertTrue(
                zip.hasEntry(expectedAccessWidener),
                "Expected ${jar.name} to contain $expectedAccessWidener"
            )
        }
    }

    private fun assertNeoForgeAccessTransformer(jar: File) {
        ZipFile(jar).use { zip ->
            assertTrue(
                zip.hasEntry("META-INF/accesstransformer.cfg"),
                "Expected ${jar.name} to contain META-INF/accesstransformer.cfg"
            )
        }
    }

    private fun assertForgeLoaderRange(jar: File, expectedLoaderVersion: String) {
        ZipFile(jar).use { zip ->
            val modsToml = zip.readEntry("META-INF/mods.toml")

            assertTrue(
                modsToml.contains("versionRange = \"[$expectedLoaderVersion,)\""),
                "Expected ${jar.name} to require Forge loader $expectedLoaderVersion. mods.toml:\n$modsToml"
            )
        }
    }

    private fun List<File>.jarNamed(name: String): File = firstOrNull { it.name == name }
        ?: error("Expected collected jar $name. Found: ${joinToString { it.name }}")

    private fun ZipFile.hasEntry(name: String): Boolean = getEntry(name) != null

    private fun ZipFile.readEntry(name: String): String {
        val entry = getEntry(name) ?: error("Expected $name in $this")
        return getInputStream(entry).bufferedReader().use { it.readText() }
    }
}
