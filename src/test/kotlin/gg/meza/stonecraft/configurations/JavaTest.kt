package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@DisplayName("Test java tooling setup")
class JavaTest : IntegrationTest {

    private lateinit var gradleTest: IntegrationTest.TestBuilder

    @BeforeEach
    fun setUp() {
        gradleTest = gradleTest()

        gradleTest.buildScript(
            """
tasks.register("checkJavaExtension") {
    doLast {
        println("sourceCompatibility=${'$'}{java.sourceCompatibility}")
        println("targetCompatibility=${'$'}{java.targetCompatibility}")
        println("toolchainVersion=${'$'}{java.toolchain.languageVersion.get()}")
    }
}
tasks.register("checkSourceSets") {
    doLast {
        java.sourceSets.forEach { sourceSet ->
            println("SourceSet: ${'$'}{sourceSet.name}")
            println("  Java sources: ${'$'}{sourceSet.java.srcDirs}")
            println("  Resources: ${'$'}{sourceSet.resources.srcDirs}")
            println("  Output: ${'$'}{sourceSet.output.dirs.files}")
        }
        println("Jar sources: ${'$'}{tasks.named<org.gradle.jvm.tasks.Jar>("jar").get().source.files}")
        val loom = project.extensions.getByType<net.fabricmc.loom.api.LoomGradleExtensionAPI>()
        println("Loom mod files: ${'$'}{loom.mods.flatMap { it.modFiles.files }}")
    }
}
            """.trimIndent()
        )
    }

    @Test
    fun `java tool versions are set to 21 correctly above`() {
        gradleTest.setStonecutterVersion("1.20.6", "fabric")
        val br = gradleTest.run("checkJavaExtension")
        gradleTest.assertNoGradleFailures(br)

        assertTrue(br.output.contains("sourceCompatibility=21"))
        assertTrue(br.output.contains("targetCompatibility=21"))
        assertTrue(br.output.contains("toolchainVersion=21"))
    }

    @Test
    fun `java tool versions are set to 17 correctly above`() {
        gradleTest.setStonecutterVersion("1.20.5", "fabric")
        val br = gradleTest.run("checkJavaExtension")
        gradleTest.assertNoGradleFailures(br)

        assertTrue(br.output.contains("sourceCompatibility=17"))
        assertTrue(br.output.contains("targetCompatibility=17"))
        assertTrue(br.output.contains("toolchainVersion=17"))
    }

    @Test
    fun `forge generated resources are jar inputs`() {
        gradleTest.setStonecutterVersion("1.21", "forge")
        gradleTest.buildScript(
            """
modSettings {
    generatedResources = layout.buildDirectory.dir("src/main/generatedForTests").get()
}"""
        )

        val expectedDirectory = gradleTest.project().layout.projectDirectory.dir("versions/1.21-forge/build/src/main/generatedForTests")
        val generatedFile = expectedDirectory.file("marker.txt").asFile.apply { parentFile.mkdirs(); writeText("generated") }
        val br = gradleTest.run("checkSourceSets")
        gradleTest.assertNoGradleFailures(br)
        assertTrue(br.output.lineSequence().any { it.startsWith("Jar sources:") && it.contains(generatedFile.absolutePath) })
        assertTrue(br.output.lineSequence().any { it.startsWith("Loom mod files:") && it.contains(expectedDirectory.asFile.absolutePath) })
        assertFalse(br.output.lineSequence().any { it.startsWith("  Output:") && it.contains(expectedDirectory.asFile.absolutePath) })
        assertFalse(br.output.lineSequence().any { it.startsWith("  Resources:") && it.contains(expectedDirectory.asFile.absolutePath) })
    }

    @Test
    fun `fabric generated resources are jar inputs`() {
        gradleTest.setStonecutterVersion("1.21", "fabric")
        gradleTest.buildScript(
            """
modSettings {
    generatedResources = layout.buildDirectory.dir("src/main/generatedForTests").get()
}"""
        )

        val expectedDirectory = gradleTest.project().layout.projectDirectory.dir("versions/1.21-fabric/build/src/main/generatedForTests")
        val generatedFile = expectedDirectory.file("marker.txt").asFile.apply { parentFile.mkdirs(); writeText("generated") }
        val br = gradleTest.run("checkSourceSets")
        gradleTest.assertNoGradleFailures(br)
        assertTrue(br.output.lineSequence().any { it.startsWith("Jar sources:") && it.contains(generatedFile.absolutePath) })
        assertTrue(br.output.lineSequence().any { it.startsWith("Loom mod files:") && it.contains(expectedDirectory.asFile.absolutePath) })
        assertFalse(br.output.lineSequence().any { it.startsWith("  Output:") && it.contains(expectedDirectory.asFile.absolutePath) })
        assertFalse(br.output.lineSequence().any { it.startsWith("  Resources:") && it.contains(expectedDirectory.asFile.absolutePath) })
    }

    @Test
    fun `modern neoforge adds isolated client and server jar inputs`() {
        gradleTest.setStonecutterVersion("1.21.4", "neoforge")
        gradleTest.buildScript(
            """
modSettings {
    generatedResources = layout.buildDirectory.dir("src/main/generatedForTests").get()
}"""
        )

        val generatedResources = gradleTest.project().layout.projectDirectory
            .dir("versions/1.21.4-neoforge/build/src/main/generatedForTests")
        val generatedFiles = listOf("client", "server").map { side ->
            generatedResources.file("$side/marker.txt").asFile.apply { parentFile.mkdirs(); writeText(side) }
        }
        val br = gradleTest.run("checkSourceSets")
        gradleTest.assertNoGradleFailures(br)

        generatedFiles.forEach { generatedFile ->
            assertTrue(br.output.lineSequence().any { it.startsWith("Jar sources:") && it.contains(generatedFile.absolutePath) })
            assertTrue(br.output.lineSequence().any { it.startsWith("Loom mod files:") && it.contains(generatedFile.parentFile.absolutePath) })
            assertFalse(br.output.lineSequence().any { it.startsWith("  Output:") && it.contains(generatedFile.parentFile.absolutePath) })
            assertFalse(br.output.lineSequence().any { it.startsWith("  Resources:") && it.contains(generatedFile.parentFile.absolutePath) })
        }
    }
}
