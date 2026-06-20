package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@DisplayName("Test the architectury quirks")
class QuirksTest : IntegrationTest {

    private lateinit var gradleTest: IntegrationTest.TestBuilder

    @BeforeEach
    fun setUp() {
        gradleTest = gradleTest().buildScript(
            """
modSettings {
    variableReplacements = mapOf(
        "custom1" to "customValue1",
        "custom2" to "customValue2",
        "custom3" to "customValue3"
    )
}

tasks.register("printClasspathServer") {
    doLast {
        tasks.named<JavaExec>("runServer").get().classpath.files.forEach {
            println("CLASSPATH_ENTRY=${'$'}{it.absolutePath}")
        }
    }
}
tasks.register("printClasspathServer1214Forge") {
    doLast {
        tasks.named<JavaExec>("runGameTestServer").get().classpath.files.forEach {
            println("CLASSPATH_ENTRY=${'$'}{it.absolutePath}")
        }
    }
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
        gradleTest.assertNoGradleFailures(br)
        val classpathEntries = br.output
            .lineSequence()
            .filter { it.startsWith("CLASSPATH_ENTRY=") }
            .toList()

        assertFalse(
            classpathEntries.any { it.contains("org.lwjgl") },
            "LWJGL was not removed from the server classpath"
        )
    }

    @Test
    fun `1241 forge gets treated accurately`() {
        gradleTest.setStonecutterVersion("1.21.4", "forge")
        gradleTest.assertNoGradleFailures(gradleTest.run("Set active project to 1.21.4-forge"))
        val br = gradleTest.run("printClasspathServer1214Forge", cacheTask = false)
        gradleTest.assertNoGradleFailures(br)
        val classpathEntries = br.output
            .lineSequence()
            .filter { it.startsWith("CLASSPATH_ENTRY=") }
            .toList()

        assertFalse(
            classpathEntries.any { it.contains("org.lwjgl") },
            "LWJGL was not removed from the server classpath"
        )
    }

    @Test
    fun `forge jopt is set properly`() {
        gradleTest.setStonecutterVersion("1.21.4", "forge")
        val br = gradleTest.run("getForcedModules")
        gradleTest.assertNoGradleFailures(br)
        assertTrue(br.output.contains("net.sf.jopt-simple:jopt-simple:5.+"), "Jopt was not set properly for forge")
    }
}
