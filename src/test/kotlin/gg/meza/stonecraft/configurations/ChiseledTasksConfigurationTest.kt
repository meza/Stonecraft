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
        gradleTest.assertNoGradleFailures(br)
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
            gradleTest.assertNoGradleFailures(br)
            expectedTasks.forEach { taskName ->
                assertTrue(br.output.contains(taskName), "Task $taskName should be present in the tasks output")
            }
        }
    }

    @Test
    fun `build and collect depends on remapJar for mapped versions`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")
        gradleTest.buildScript(
            """
            tasks.register("printBuildAndCollectDeps") {
                doLast {
                    val task = tasks.named("buildAndCollect").get()
                    val deps = task.taskDependencies.getDependencies(task).map { it.name }.sorted()
                    deps.forEach { println("buildAndCollect.dep=" + it) }
                }
            }
            """.trimIndent()
        )

        val br = gradleTest.run("printBuildAndCollectDeps")
        gradleTest.assertNoGradleFailures(br)
        assertTrue(
            br.output.contains("buildAndCollect.dep=remapJar"),
            "Expected buildAndCollect to depend on remapJar for mapped versions."
        )
    }

    @Test
    fun `build and collect depends on jar for deobfuscated versions`() {
        gradleTest.setStonecutterVersion("26.1", "fabric")
        gradleTest.buildScript(
            """
            loom {
                accessWidenerPath = rootProject.layout.projectDirectory.file("src/main/resources/examplemod.deobfuscated.accesswidener")
            }

            tasks.register("printBuildAndCollectDeps") {
                doLast {
                    val task = tasks.named("buildAndCollect").get()
                    val deps = task.taskDependencies.getDependencies(task).map { it.name }.sorted()
                    deps.forEach { println("buildAndCollect.dep=" + it) }
                }
            }
            """.trimIndent()
        )

        val br = gradleTest.run("printBuildAndCollectDeps")
        gradleTest.assertNoGradleFailures(br)
        assertTrue(
            br.output.contains("buildAndCollect.dep=jar"),
            "Expected buildAndCollect to depend on jar for deobfuscated versions."
        )
    }

    @Test
    fun `neoforge test tasks include minecraft unit test settings`() {
        gradleTest.setStonecutterVersion("26.1", "neoforge")
        gradleTest.buildScript(
            """
            loom {
                accessWidenerPath = rootProject.layout.projectDirectory.file("src/main/resources/examplemod.deobfuscated.accesswidener")
            }

            tasks.register("printTestTaskSettings") {
                doLast {
                    tasks.withType(org.gradle.api.tasks.testing.Test::class.java).forEach { testTask ->
                        println("test.jvmArgs=" + testTask.jvmArgs.joinToString("|"))
                        println("test.fml.modFolders=" + testTask.systemProperties["fml.modFolders"])
                    }
                }
            }
            """.trimIndent()
        )

        val br = gradleTest.run("printTestTaskSettings")
        gradleTest.assertNoGradleFailures(br)
        val versionProject = "versions/26.1-neoforge"
        val expectedFolders = listOf(
            "main%%${gradleTest.project().layout.projectDirectory.dir("$versionProject/build/resources/main").asFile.absolutePath}",
            "main%%${gradleTest.project().layout.projectDirectory.dir("$versionProject/build/classes/java/main").asFile.absolutePath}",
            "main%%${gradleTest.project().layout.projectDirectory.dir("$versionProject/build/classes/java/test").asFile.absolutePath}"
        )

        assertTrue(
            br.output.contains("test.jvmArgs=--add-opens=java.base/java.lang.invoke=ALL-UNNAMED"),
            "NeoForge Test tasks should open java.lang.invoke for minecraft-facing unit tests."
        )
        expectedFolders.forEach { folder ->
            assertTrue(
                br.output.contains(folder),
                "NeoForge Test tasks should include $folder in fml.modFolders."
            )
        }
    }
}
