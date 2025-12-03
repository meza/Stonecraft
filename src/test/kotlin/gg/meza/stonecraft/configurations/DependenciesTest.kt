package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Test that the dependencies are correctly resolved from the versions/dependencies/[minecraftVersion].properties files
 * and that the dependencies are correctly added to the project
 *
 */
@DisplayName("Test dependency setup")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DependenciesTest : IntegrationTest {
    private lateinit var gradleTest: IntegrationTest.TestBuilder

    @BeforeEach
    fun setUp() {
        gradleTest = gradleTest().buildScript(
            """
tasks.register("printDeps") {
    doLast {
        configurations.forEach { config ->
            println("Configuration: ${'$'}{config.name}")
            config.allDependencies.forEach { dep ->
                println("  ${'$'}dep")
            }
        }
    }
}
            """.trimIndent()
        )
    }

    @Test
    fun `dependencies are correctly added for all subprojects`() {
        gradleTest.setStonecutterVersion("1.21", "fabric", "forge", "neoforge")
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge", "neoforge")

        val buildResult = gradleTest.run("printDeps")

        val expectedDependencies = listOf(
            "net.fabricmc.fabric-api:fabric-api:0.114.0+1.21.4",
            "net.fabricmc.fabric-api:fabric-api:0.102.0+1.21",
            "net.minecraftforge:forge:1.21.4-54.0.16",
            "net.minecraftforge:forge:1.21-51.0.33",
            "net.neoforged:neoforge:21.0.167",
            "net.neoforged:neoforge:21.4.47-beta"
        )

        expectedDependencies.forEach { dependency ->
            assertTrue(
                buildResult.output.contains(dependency),
                "Dependency $dependency was not resolved from the versions deps"
            )
        }
    }

    @Test
    fun `fabric only dependencies are added to the project`() {
        gradleTest.setStonecutterVersion("1.21", "fabric")
        gradleTest.setStonecutterVersion("1.21.4", "fabric")

        val buildResult = gradleTest.run("printDeps")

        val expectedDependencies = listOf(
            "net.fabricmc.fabric-api:fabric-api:0.114.0+1.21.4",
            "net.fabricmc.fabric-api:fabric-api:0.102.0+1.21"
        )

        val notExpectedDependencies = listOf(
            "net.minecraftforge:forge",
            "net.neoforged:neoforge"
        )

        expectedDependencies.forEach { dependency ->
            assertTrue(
                buildResult.output.contains(dependency),
                "Dependency $dependency was not resolved from the versions deps"
            )
        }

        notExpectedDependencies.forEach { dependency ->
            assertFalse(
                buildResult.output.contains(dependency),
                "Dependency $dependency was included in the dependencies even though it should not have been"
            )
        }
    }

    @Test
    fun `only the required single forge dependency is added`() {
        gradleTest.setStonecutterVersion("1.21", "forge")

        val buildResult = gradleTest.run("printDeps")

        val expectedDependencies = listOf(
            "net.minecraftforge:forge:1.21-51.0.33"
        )

        val notExpectedDependencies = listOf(
            "net.fabricmc.fabric",
            "net.neoforged:neoforge"
        )

        expectedDependencies.forEach { dependency ->
            assertTrue(
                buildResult.output.contains(dependency),
                "Dependency $dependency was not resolved from the versions deps"
            )
        }

        notExpectedDependencies.forEach { dependency ->
            assertFalse(
                buildResult.output.contains(dependency),
                "Dependency $dependency was included in the dependencies even though it should not have been"
            )
        }
    }

    @Test
    fun `the minecraft version can be overridden`() {
        gradleTest.setStonecutterVersion("1.21.6", "fabric")
        val buildResult = gradleTest.run("printDeps")

        assertTrue(
            buildResult.output.contains("com.mojang:minecraft:1.21.6-rc1"),
            "Dependency \"com.mojang:minecraft:1.21.6-rc1\" was not resolved from the versions deps"
        )
    }

    @Test
    fun `legacy yarn mappings are used when configured`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "neoforge")

        val buildResult = gradleTest.run("printDeps")

        assertTrue(
            buildResult.output.contains("net.fabricmc:yarn"),
            "Yarn mappings should be used when the legacy property is present."
        )
        assertFalse(
            buildResult.output.contains("net.minecraft:mappings"),
            "Mojmap should not replace Yarn when the legacy property is present."
        )
    }

    @Test
    fun `mojmap is the default when yarn mappings are missing`() {
        gradleTest.setStonecutterVersion("1.21.6", "fabric")

        val buildResult = gradleTest.run("printDeps")

        assertTrue(
            buildResult.output.contains("loom.mappings.1_21_6"),
            "Official Mojmap mappings should be used when no Yarn mappings are configured."
        )
    }
}
