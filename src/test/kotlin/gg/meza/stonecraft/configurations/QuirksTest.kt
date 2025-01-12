package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Test the architectury quirks")
class QuirksTest : IntegrationTest {

    private lateinit var gradleTest: IntegrationTest.TestBuilder

    @BeforeEach
    fun setUp() {
        gradleTest = gradleTest().buildScript(
            """
tasks.register("printClasspathServer") {
    tasks.named<JavaExec>("runServer").get().classpath.forEach(${':'}${':'}println)
    tasks.named<JavaExec>("runGameTestServer").get().classpath.forEach(${':'}${':'}println)
}
tasks.register("printClasspathServer1214Forge") {
    tasks.named<JavaExec>("runServer").get().classpath.forEach(${':'}${':'}println)
    dependsOn("runGameTestServer")
}
tasks.register("getForcedModules") {
    configurations.forEach {
        println(it.resolutionStrategy.forcedModules)
    }
}
            """.trimIndent()
        )
    }

    @Test
    fun `lwjgl is not present in the server`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")
        val br = gradleTest.run("printClasspathServer")

        assertFalse(br.output.contains("org.lwjgl"), "LWJGL was not removed from the server classpath")
    }

    @Test
    fun `1241 forge gets treated accurately`() {
        gradleTest.setStonecutterVersion("1.21.4", "forge")
        val br = gradleTest.run("printClasspathServer1214Forge")

        assertFalse(br.output.contains("org.lwjgl"), "LWJGL was not removed from the server classpath")
        assertTrue(br.output.contains("Game test server task is disabled for 1.21.4 Forge Architectury"))
    }

    @Test
    fun `forge jopt is set properly`() {
        gradleTest.setStonecutterVersion("1.21.4", "forge")
        val br = gradleTest.run("getForcedModules")

        assertTrue(br.output.contains("net.sf.jopt-simple:jopt-simple:5.+"), "Jopt was not set properly for forge")
    }
}
