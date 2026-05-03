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
    fun `new rc minecraft versions are picked up from version properties`() {
        gradleTest.setStonecutterVersion("600.21.11", "fabric")

        val buildResult = gradleTest.run("printDeps")

        assertTrue(
            buildResult.output.contains("com.mojang:minecraft:1.21.11-rc2"),
            "Dependency \"com.mojang:minecraft:1.21.11-rc2\" was not resolved from the versions deps"
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

    @Test
    fun `deobfuscated versions skip mappings and use implementation for fabric loader`() {
        gradleTest.setStonecutterVersion("26.1", "fabric")
        gradleTest.buildScript(
            """
tasks.register("printConfigDeps") {
    doLast {
        listOf("implementation", "modImplementation", "mappings").forEach { name ->
            val config = configurations.findByName(name)
            if (config != null) {
                config.allDependencies.forEach { dep ->
                    println("dep." + name + "=" + dep)
                }
            }
        }
    }
}
            """.trimIndent()
        )

        val buildResult = gradleTest.run("printConfigDeps")

        assertTrue(
            buildResult.output.contains("dep.implementation=net.fabricmc:fabric-loader:0.18.4"),
            "Fabric loader should be added to implementation for deobfuscated versions."
        )
        assertFalse(
            buildResult.output.contains("dep.modImplementation=net.fabricmc:fabric-loader:0.18.4"),
            "Fabric loader should not be added to modImplementation for deobfuscated versions."
        )
        assertFalse(
            buildResult.output.contains("dep.mappings="),
            "No mappings should be added for deobfuscated versions."
        )
        assertFalse(
            buildResult.output.contains("net.fabricmc:yarn"),
            "Yarn mappings should be ignored for deobfuscated versions."
        )
        assertFalse(
            buildResult.output.contains("loom.mappings"),
            "Mojmap mappings should be skipped for deobfuscated versions."
        )
    }

    @Test
    fun `yarn mappings are ignored for deobfuscated versions with a warning`() {
        gradleTest.setStonecutterVersion("26.1", "fabric")
        gradleTest.setModProperty("yarn_mappings", "1.21.4+build.4")

        val buildResult = gradleTest.run("printDeps")

        assertTrue(
            buildResult.output.contains(
                "Ignoring yarn_mappings for Minecraft 26.1-snapshot-1; mappings are not supported past 1.21.11."
            ),
            "A warning should be logged when yarn_mappings is set for deobfuscated versions."
        )
        assertFalse(
            buildResult.output.contains("net.fabricmc:yarn"),
            "Yarn mappings should be ignored for deobfuscated versions."
        )
    }
}
