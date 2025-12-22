package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@DisplayName("Test stonecutter task setup")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChiseledTasksConfigurationTest : IntegrationTest {

    private lateinit var gradleTest: IntegrationTest.TestBuilder

    @BeforeEach
    fun setUp() {
        gradleTest = gradleTest()
    }

    @Test
    fun `chiseled build and collect delegates to build and collect`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")
        gradleTest.buildScript(
            """
            tasks.register("printChiseledBuildAndCollectDeps") {
                doLast {
                    val chiseled = rootProject.tasks.named("chiseledBuildAndCollect").get()
                    val deps = chiseled.taskDependencies.getDependencies(chiseled).map { it.path }.sorted()
                    deps.forEach { println("chiseled.dep=" + it) }
                }
            }
            """.trimIndent()
        )

        val br = gradleTest.run("printChiseledBuildAndCollectDeps")
        assertTrue(
            Regex("chiseled\\.dep=.*:buildAndCollect").containsMatchIn(br.output),
            "Expected chiseledBuildAndCollect to depend on buildAndCollect. Output was:\n${br.output}"
        )
    }

    @Test
    fun `stonecutter tasks are configured for 1-21-4`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "neoforge")
        // Check that the chiseled tasks are listed in the tasks output
        val expectedTasks = listOf(
            "buildAndCollect",
            "chiseledBuild",
            "chiseledClean",
            "chiseledDatagen",
            "chiseledTest",
            "chiseledGameTest",
            "chiseledBuildAndCollect",
            "chiseledPublishMods",
            "runClient",
            "runServer",
            "runGameTestClient",
            "runGameTestServer",
            "runDatagen",
            "buildActive",
            "runActive",
            "runActiveServer",
            "dataGenActive",
            "testActiveClient",
            "testActiveServer",
            "chiseledPublishMods",
            "Set active project to 1.21.4-fabric",
            "Set active project to 1.21.4-neoforge"
        )

        @Test
        fun `stonecutter tasks are configured for 1-20-4`() {
            gradleTest.setStonecutterVersion("1.20", "fabric", "forge")
            // Check that the chiseled tasks are listed in the tasks output
            val expectedTasks = listOf(
                "buildAndCollect",
                "chiseledBuild",
                "chiseledClean",
                "chiseledDatagen",
                "chiseledTest",
                "chiseledGameTest",
                "chiseledBuildAndCollect",
                "chiseledPublishMods",
                "runClient",
                "runServer",
                "runGameTestClient",
                "runGameTestServer",
                "runDatagen",
                "buildActive",
                "runActive",
                "runActiveServer",
                "dataGenActive",
                "testActiveClient",
                "testActiveServer",
                "chiseledPublishMods",
                "Set active project to 1.20-fabric",
                "Set active project to 1.20-forge"
            )

            val br = gradleTest.run("tasks")
            expectedTasks.forEach { taskName ->
                assertTrue(br.output.contains(taskName), "Task $taskName should be present in the tasks output")
            }
        }
    }
}
