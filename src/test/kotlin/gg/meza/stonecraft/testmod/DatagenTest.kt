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
    fun `testmod chiseled datagen generates stone advancement for every version project`() {
        val gradleTest = gradleTestMod()
        deleteCopiedGeneratedResources(gradleTest)

        val result = gradleTest.run(
            listOf("--no-configuration-cache", "chiseledDatagen"),
            cacheTask = false
        )

        assertNoGradleFailures(result.output)

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

    private fun assertNoGradleFailures(output: String) {
        val failureMarkers = listOf(
            "BUILD FAILED",
            "FAILURE: Build failed",
            "There were failing tests",
        )

        failureMarkers.forEach { marker ->
            assertTrue(
                !output.contains(marker),
                "Expected no Gradle failure marker '$marker'. Output:\n$output"
            )
        }

        assertTrue(
            !Regex("""> Task .+ FAILED""").containsMatchIn(output),
            "Expected no failed Gradle tasks. Output:\n$output"
        )
    }
}
