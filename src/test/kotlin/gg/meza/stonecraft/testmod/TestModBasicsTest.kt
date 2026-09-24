package gg.meza.stonecraft.testmod

import gg.meza.stonecraft.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

@DisplayName("Testmod e2e")
class TestModBasicsTest : IntegrationTest {
    private val versionProjects = listOf(
        "1.20.4-fabric", "1.20.4-forge", "1.20.4-neoforge",
        "26.1-fabric", "26.1-forge", "26.1-neoforge"
    )

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
    @Timeout(value = 20, unit = TimeUnit.MINUTES)
    fun `testmod generates data, builds jars, and runs gametests for every version project`() {
        val gradleTest = gradleTestMod()
        deleteCopiedGeneratedResources(gradleTest)

        val result = gradleTest.run(
            listOf("--no-configuration-cache", "chiseledDatagen", "buildAndCollect", "chiseledGameTest"),
            cacheTask = false
        )

        gradleTest.assertNoGradleFailures(result)

        versionProjects.forEach { versionProject ->
            listOf("runDatagen", "buildAndCollect", "runGameTestServer").forEach { taskName ->
                val taskPath = ":$versionProject:$taskName"
                assertTrue(
                    result.task(taskPath)?.outcome == TaskOutcome.SUCCESS,
                    "Expected $taskPath to execute successfully; outcome was ${result.task(taskPath)?.outcome}"
                )
            }

            val generatedDirectory = File(gradleTest.project().projectDir, "versions/$versionProject/src/main/generated")
            assertTrue(
                generatedDirectory.walkTopDown().any { it.isFile && it.name == "stone.json" },
                "Expected $versionProject to generate stone.json under $generatedDirectory"
            )

            val gameTestTask = ":$versionProject:runGameTestServer"
            val taskStart = result.output.indexOf("> Task $gameTestTask")
            assertTrue(taskStart >= 0, "Expected output for $gameTestTask")
            val nextTask = result.output.indexOf("> Task ", taskStart + 1).let { if (it < 0) result.output.length else it }
            val taskOutput = result.output.substring(taskStart, nextTask)
            assertTrue(
                Regex("All [1-9][0-9]* required tests passed").containsMatchIn(taskOutput),
                "Expected $gameTestTask to run at least one required game test. Output:\n$taskOutput"
            )
        }

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

        versionProjects.forEach { versionProject ->
            val (version, loader) = versionProject.split("-")
            val jar = collectedJars.jarNamed("stonecraft_testmod-$loader-0.0-SNAPSHOT+mc$version.jar")
            val advancementDirectory = if (version == "1.20.4") "advancements" else "advancement"
            val generatedEntry = "data/stonecraft_testmod/$advancementDirectory/datagen/stone.json"
            ZipFile(jar).use { zip ->
                assertEquals(
                    1,
                    zip.entries().asSequence().count { it.name == generatedEntry },
                    "Expected exactly one fresh generated advancement in ${jar.name}: $generatedEntry"
                )
            }
        }

        val clientDatagen = ":26.1-neoforge:runClientDatagen"
        val serverDatagen = ":26.1-neoforge:runServerDatagen"
        val executedTasks = result.tasks.map { it.path }
        assertTrue(
            executedTasks.indexOf(clientDatagen) >= 0 &&
                executedTasks.indexOf(clientDatagen) < executedTasks.indexOf(serverDatagen),
            "Expected NeoForge client datagen before server datagen. Task order: $executedTasks"
        )

        val neoforgeProject = File(gradleTest.project().projectDir, "versions/26.1-neoforge")
        val clientAdvancement = File(
            neoforgeProject,
            "src/main/generated/client/data/stonecraft_testmod/advancement/datagen/stone.json"
        )
        assertTrue(clientAdvancement.isFile, "Expected NeoForge server datagen to preserve the client advancement")

    }

    private fun deleteCopiedGeneratedResources(gradleTest: IntegrationTest.TestBuilder) {
        versionProjects.forEach { versionProject ->
            File(gradleTest.project().projectDir, "versions/$versionProject/src/main/generated")
                .deleteRecursively()
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
