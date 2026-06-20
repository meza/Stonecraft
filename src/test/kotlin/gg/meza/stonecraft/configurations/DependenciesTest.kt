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
        gradleTest.assertNoGradleFailures(buildResult)

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
        gradleTest.assertNoGradleFailures(buildResult)

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
        gradleTest.assertNoGradleFailures(buildResult)

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
        gradleTest.assertNoGradleFailures(buildResult)

        assertTrue(
            buildResult.output.contains("com.mojang:minecraft:1.21.6-rc1"),
            "Dependency \"com.mojang:minecraft:1.21.6-rc1\" was not resolved from the versions deps"
        )
    }

    @Test
    fun `new rc minecraft versions are picked up from version properties`() {
        gradleTest.setStonecutterVersion("600.21.11", "fabric")

        val buildResult = gradleTest.run("printDeps")
        gradleTest.assertNoGradleFailures(buildResult)

        assertTrue(
            buildResult.output.contains("com.mojang:minecraft:1.21.11-rc2"),
            "Dependency \"com.mojang:minecraft:1.21.11-rc2\" was not resolved from the versions deps"
        )
    }

    @Test
    fun `legacy yarn mappings are used when configured`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "neoforge")

        val buildResult = gradleTest.run("printDeps")
        gradleTest.assertNoGradleFailures(buildResult)

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
        gradleTest.assertNoGradleFailures(buildResult)

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
loom {
    accessWidenerPath = rootProject.layout.projectDirectory.file("src/main/resources/examplemod.deobfuscated.accesswidener")
}
            """.trimIndent()
        )
        gradleTest.buildScript(
            """
tasks.register("printConfigDeps") {
    doLast {
        listOf("implementation", "modImplementation", "mappings", "testImplementation").forEach { name ->
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
        gradleTest.assertNoGradleFailures(buildResult)

        assertTrue(
            buildResult.output.contains("dep.implementation=net.fabricmc:fabric-loader:0.19.2"),
            "Fabric loader should be added to implementation for deobfuscated versions."
        )
        assertFalse(
            buildResult.output.contains("dep.modImplementation=net.fabricmc:fabric-loader:0.19.2"),
            "Fabric loader should not be added to modImplementation for deobfuscated versions."
        )
        assertTrue(
            buildResult.output.contains("dep.testImplementation=net.fabricmc:fabric-loader-junit:0.19.2"),
            "Fabric loader JUnit support should be added to testImplementation for deobfuscated versions."
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
        gradleTest.buildScript(
            """
loom {
    accessWidenerPath = rootProject.layout.projectDirectory.file("src/main/resources/examplemod.deobfuscated.accesswidener")
}
            """.trimIndent()
        )
        gradleTest.setModProperty("yarn_mappings", "1.21.4+build.4")

        val buildResult = gradleTest.run("printDeps")
        gradleTest.assertNoGradleFailures(buildResult)

        assertTrue(
            buildResult.output.contains(
                "Ignoring yarn_mappings for Minecraft 26.1; mappings are not supported past 1.21.11."
            ),
            "A warning should be logged when yarn_mappings is set for deobfuscated versions."
        )
        assertFalse(
            buildResult.output.contains("net.fabricmc:yarn"),
            "Yarn mappings should be ignored for deobfuscated versions."
        )
    }

    @Test
    fun `neoforge test runtime dependencies include game test fixtures`() {
        gradleTest.setStonecutterVersion("26.1", "neoforge")
        gradleTest.buildScript(
            """
loom {
    accessWidenerPath = rootProject.layout.projectDirectory.file("src/main/resources/examplemod.deobfuscated.accesswidener")
}
            """.trimIndent()
        )
        gradleTest.buildScript(
            """
tasks.register("printTestRuntimeDeps") {
    doLast {
        configurations.getByName("testRuntimeOnly").allDependencies.forEach { dep ->
            println("testRuntimeOnly.dep=" + dep)
            if (dep is org.gradle.api.artifacts.ModuleDependency) {
                dep.requestedCapabilities.forEach { capability ->
                    println("testRuntimeOnly.capability=" + capability.group + ":" + capability.name)
                }
            }
        }
    }
}
            """.trimIndent()
        )

        val buildResult = gradleTest.run("printTestRuntimeDeps")
        gradleTest.assertNoGradleFailures(buildResult)

        assertTrue(
            buildResult.output.contains("testRuntimeOnly.dep=net.neoforged:neoforge:26.1.0.19-beta"),
            "NeoForge should be added to testRuntimeOnly for its test fixtures."
        )
        assertTrue(
            buildResult.output.contains(
                "testRuntimeOnly.capability=net.neoforged:neoforge-moddev-test-fixtures"
            ),
            "NeoForge test runtime should request the moddev test fixtures capability."
        )
        assertTrue(
            buildResult.output.contains("testRuntimeOnly.dep=org.junit.platform:junit-platform-launcher"),
            "NeoForge test runtime should include the JUnit Platform launcher."
        )
    }
}
