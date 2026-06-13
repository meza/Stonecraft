package gg.meza.stonecraft.testmod

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

@DisplayName("Testmod build")
class TestModBasicsTest : IntegrationTest {

    @Test
    fun `testmod can build and collect jars`() {
        val gradleTest = gradleTestMod()

        val result = gradleTest.run("buildAndCollect", cacheTask = false)

        assertNoGradleFailures(result.output)

        val collectedJars = gradleTest.project()
            .layout.projectDirectory
            .dir("build/libs")
            .asFile
            .listFiles { file -> file.extension == "jar" }
            .orEmpty()

        assertTrue(
            collectedJars.any { file -> file.name.startsWith("stonecraft_testmod-") },
            "Expected buildAndCollect to collect testmod jars. Found: ${collectedJars.joinToString { it.name }}"
        )
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    fun `testmod can run chiseled gametest`() {
        val gradleTest = gradleTestMod()

        val result = gradleTest.run("chiseledGameTest", cacheTask = false)

        assertNoGradleFailures(result.output)
    }

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
