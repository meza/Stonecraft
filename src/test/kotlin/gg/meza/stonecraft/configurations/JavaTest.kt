package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

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
        }
    }
}
            """.trimIndent()
        )
    }

    @Test
    fun `java tool versions are set to 21 correctly above`() {
        gradleTest.setStonecutterVersion("1.20.6", "fabric")
        val br = gradleTest.run("checkJavaExtension")

        assertTrue(br.output.contains("sourceCompatibility=21"))
        assertTrue(br.output.contains("targetCompatibility=21"))
        assertTrue(br.output.contains("toolchainVersion=21"))
    }

    @Test
    fun `java tool versions are set to 17 correctly above`() {
        gradleTest.setStonecutterVersion("1.20.5", "fabric")
        val br = gradleTest.run("checkJavaExtension")

        assertTrue(br.output.contains("sourceCompatibility=17"))
        assertTrue(br.output.contains("targetCompatibility=17"))
        assertTrue(br.output.contains("toolchainVersion=17"))
    }

    @Disabled("architectury-loom issue #274")
    @Test
    fun `check if the generated source is added for forge`() {
        gradleTest.setStonecutterVersion("1.21", "forge")
        gradleTest.buildScript(
            """
modSettings {
    generatedResources = layout.buildDirectory.dir("src/main/generatedForTests").get()
}"""
        )

        val br = gradleTest.run("checkSourceSets")
        val expectedDirectory = gradleTest.project().layout.projectDirectory.dir("versions/1.21-forge/build/src/main/generatedForTests")
        assertTrue(br.output.contains(expectedDirectory.asFile.absolutePath))
    }

    @Test
    fun `check if the generated source is NOT added for fabric`() {
        gradleTest.setStonecutterVersion("1.21", "fabric")
        gradleTest.buildScript(
            """
modSettings {
    generatedResources = layout.buildDirectory.dir("src/main/generatedForTests").get()
}"""
        )

        val br = gradleTest.run("checkSourceSets")
        val expectedDirectory = gradleTest.project().layout.projectDirectory.dir("versions/1.21-forge/build/src/main/generatedForTests")
        assertFalse(br.output.contains(expectedDirectory.asFile.absolutePath))
    }
}
