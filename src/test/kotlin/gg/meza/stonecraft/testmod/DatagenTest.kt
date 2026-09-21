package gg.meza.stonecraft.testmod

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.io.File
import java.util.concurrent.TimeUnit

@DisplayName("Testmod datagen")
class DatagenTest : IntegrationTest {

    @Test
    fun `modern neoforge server datagen preserves client generated resources`() {
        val gradleTest = gradleTestMod()
        deleteCopiedGeneratedResources(gradleTest)

        val aggregate = gradleTest.run(
            listOf("--dry-run", "--no-configuration-cache", ":26.1-neoforge:runDatagen"),
            cacheTask = false
        )
        gradleTest.assertNoGradleFailures(aggregate)
        assertTrue(aggregate.output.contains(":26.1-neoforge:runClientDatagen SKIPPED"))
        assertTrue(aggregate.output.contains(":26.1-neoforge:runServerDatagen SKIPPED"))

        val clientDatagen = gradleTest.run(
            listOf("--no-configuration-cache", ":26.1-neoforge:runClientDatagen"),
            cacheTask = false
        )
        gradleTest.assertNoGradleFailures(clientDatagen)

        val versionProject = File(gradleTest.project().projectDir, "versions/26.1-neoforge")
        val generatedAdvancement = File(
            versionProject,
            "src/main/generated/client/data/stonecraft_testmod/advancement/datagen/stone.json"
        )
        assertTrue(
            generatedAdvancement.isFile,
            "Expected client datagen to produce the advancement"
        )

        val serverDatagen = gradleTest.run(
            listOf("--no-configuration-cache", ":26.1-neoforge:runServerDatagen"),
            cacheTask = false
        )
        gradleTest.assertNoGradleFailures(serverDatagen)
        assertTrue(
            generatedAdvancement.isFile,
            "Expected the client-generated advancement to survive server datagen"
        )

        val processResources = gradleTest.run(
            listOf("--no-configuration-cache", ":26.1-neoforge:processResources"),
            cacheTask = false
        )
        gradleTest.assertNoGradleFailures(processResources)

        val processedAdvancement = File(
            versionProject,
            "build/resources/main/data/stonecraft_testmod/advancement/datagen/stone.json"
        )
        assertTrue(
            processedAdvancement.isFile,
            "Expected the client output root to be included in processed resources"
        )
    }

    @Test
    fun `testmod chiseled datagen generates stone advancement for every version project`() {
        val gradleTest = gradleTestMod()
        deleteCopiedGeneratedResources(gradleTest)

        val result = gradleTest.run(
            listOf("--no-configuration-cache", "chiseledDatagen"),
            cacheTask = false
        )

        gradleTest.assertNoGradleFailures(result)

        val versionProjects = versionProjectDirectories(
            result.output,
            gradleTest.project().projectDir
        )

        assertTrue(
            versionProjects.isNotEmpty(),
            "Expected Gradle to report at least one version project. Output:\n${result.output}"
        )

        versionProjects.forEach { versionProject ->
            val generatedDirectory = File(versionProject, "src/main/generated")
            val stoneJsonFiles = generatedDirectory
                .walkTopDown()
                .filter { file -> file.isFile && file.name == "stone.json" }
                .toList()

            assertTrue(
                stoneJsonFiles.isNotEmpty(),
                "Expected ${versionProject.name} to generate stone.json under $generatedDirectory"
            )
        }
    }

    private fun deleteCopiedGeneratedResources(gradleTest: IntegrationTest.TestBuilder) {
        gradleTest.project()
            .layout.projectDirectory
            .dir("versions")
            .asFile
            .listFiles()
            .orEmpty()
            .map { versionProject -> File(versionProject, "src/main/generated") }
            .filter { generatedDirectory -> generatedDirectory.exists() }
            .forEach { generatedDirectory -> generatedDirectory.deleteRecursively() }
    }

    private fun versionProjectDirectories(output: String, projectDirectory: File): List<File> = Regex("""(?m)^> Configure project :(.+-.+)$""")
        .findAll(output)
        .map { match -> match.groupValues[1] }
        .distinct()
        .map { projectName -> File(projectDirectory, "versions/$projectName") }
        .toList()
}
