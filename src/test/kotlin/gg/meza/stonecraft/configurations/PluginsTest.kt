package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@DisplayName("Test plugin setup")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PluginsTest : IntegrationTest {

    private lateinit var gradleTest: IntegrationTest.TestBuilder

    @BeforeEach
    fun setUp() {
        gradleTest = gradleTest().buildScript(
            """
tasks.register("printPlugins") {
    doLast {
        plugins.forEach { plugin ->
            println(plugin.javaClass)
        }
    }
}
            """.trimIndent()
        )
    }

    @Test
    fun `plugin throws an error if architectury exists`() {
        gradleTest = gradleTest(false).buildScript(
            """
plugins {
    id("dev.architectury.loom")
    id("gg.meza.stonecraft")
}
            """.trimIndent()
        )
        val br = gradleTest.run("printPlugins")

        assertTrue(br.output.contains("IllegalStateException: This plugin needs to be applied before the Architectury Loom plugin"))
        assertTrue(br.output.contains("Please move gg.meza.stonecraft plugin to the top of your build.gradle.kts file"))
    }

    @Test
    fun `plugins are properly applied`() {
        val br = gradleTest.run("printPlugins")

        assertTrue(br.output.contains("dev.kikugie.stonecutter.StonecutterPlugin"))
        assertTrue(br.output.contains("net.fabricmc.loom.LoomRepositoryPlugin"))
        assertTrue(br.output.contains("net.fabricmc.loom.LoomGradlePlugin"))
        assertTrue(br.output.contains("me.modmuss50.mpp.MppPlugin"))
        assertTrue(br.output.contains("Architectury Loom"))
    }

    @Test
    fun `eol warning is printed by default`() {
        val br = gradleTest.run("printPlugins")
        assertTrue(br.output.contains("Stonecraft 1.7.x is end-of-life"))
        assertTrue(br.output.contains("-Pstonecraft.eolWarning=false"))
    }

    @Test
    fun `eol warning can be disabled`() {
        val br = gradleTest()
            .withProperties(mapOf("stonecraft.eolWarning" to "false"))
            .buildScript(
                """
tasks.register("printPlugins") {
    doLast {
        plugins.forEach { plugin ->
            println(plugin.javaClass)
        }
    }
}
                """.trimIndent()
            )
            .run("printPlugins")

        assertFalse(br.output.contains("Stonecraft 1.7.x is end-of-life"))
    }

    @Test
    fun `loom platform correctly gets applied`() {
        gradleTest.buildScript(
            """
tasks.register("printPlatform") {
    doLast {
        println("loom.platform is set to "+ext["loom.platform"])
    }
}
            """.trimIndent()
        )

        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge", "neoforge")

        val br = gradleTest.run("printPlatform")

        assertTrue(br.output.contains("loom.platform is set to fabric"))
        assertTrue(br.output.contains("loom.platform is set to forge"))
        assertTrue(br.output.contains("loom.platform is set to neoforge"))
    }
}
