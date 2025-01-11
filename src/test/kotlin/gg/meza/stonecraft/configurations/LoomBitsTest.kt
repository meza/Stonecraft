package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import okio.Path
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@DisplayName("Test loom setup")
class LoomBitsTest: IntegrationTest {

    private lateinit var gradleTest: IntegrationTest.TestBuilder

    @BeforeEach
    fun setUp() {
        gradleTest = gradleTest().buildScript(LoomFullTest.loomTask)
    }

    @Test
    fun `test access wideners when there is no access widener in the project`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge")
        gradleTest.project().layout.projectDirectory.file("src/main/resources/examplemod.accesswidener").asFile.delete()
        val br = gradleTest.run("printLoomSettings")

        assertFalse(br.output.contains("loom.accessWidenerPath"))
        assertFalse(br.output.contains("forge.convertAccessWideners"))

    }

    @Test
    fun `test access wideners when there is an access widener in the project`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge")
        val br = gradleTest.run("printLoomSettings")
        val awPath = gradleTest.project().layout.projectDirectory.file("src/main/resources/examplemod.accesswidener").asFile.absolutePath

        val awString = "loom.accessWidenerPath=\"$awPath\""
        val pathCount = Regex(Regex.escape(awString)).findAll(br.output).count()
        val forgeString = "forge.convertAccessWideners=\"true\""
        val forgeCount = Regex(Regex.escape(forgeString)).findAll(br.output).count()

        assertTrue(br.output.contains(awString))
        assertTrue(br.output.contains(forgeString))
        assertEquals(2, pathCount, "Expected 2 occurrences of $awString, found $pathCount")
        assertEquals(1, forgeCount, "Expected 1 occurrence of $forgeString, found $forgeCount")

    }

    @Test
    fun `test with a custom run directory`() {
        gradleTest.setStonecutterVersion("1.21", "fabric", "forge", "neoforge")
        gradleTest.buildScript("""
            modSettings {
                runDirectory = rootProject.layout.projectDirectory.dir("run-test")
            }
        """.trimIndent())

        val runDir = gradleTest.project().layout.projectDirectory.dir("versions/1.20.2-fabric").asFile.toPath().relativize(gradleTest.project().layout.projectDirectory.dir("run-test").asFile.toPath())
        val testClientDir = gradleTest.project().layout.projectDirectory.dir("versions/1.20.2-fabric").asFile.toPath().relativize(gradleTest.project().layout.projectDirectory.dir("run-test/testclient").asFile.toPath())
        val testServerDir = gradleTest.project().layout.projectDirectory.dir("versions/1.20.2-fabric").asFile.toPath().relativize(gradleTest.project().layout.projectDirectory.dir("run-test/testserver").asFile.toPath())

        val result = gradleTest.run("printLoomSettings")

        assertTrue(result.output.contains("[1.21-fabric] client runDir=$runDir"))
        assertTrue(result.output.contains("[1.21-fabric] datagen runDir=build/datagen"))
        assertTrue(result.output.contains("[1.21-fabric] gameTestClient runDir=${testClientDir}${Path.DIRECTORY_SEPARATOR}fabric"))
        assertTrue(result.output.contains("[1.21-fabric] gameTestServer runDir=${testServerDir}${Path.DIRECTORY_SEPARATOR}fabric"))
        assertTrue(result.output.contains("[1.21-fabric] server runDir=$runDir"))

        assertTrue(result.output.contains("[1.21-forge] client runDir=$runDir"))
        assertTrue(result.output.contains("[1.21-forge] datagen runDir=$runDir"))
        assertTrue(result.output.contains("[1.21-forge] gameTestClient runDir=${testClientDir}${Path.DIRECTORY_SEPARATOR}forge"))
        assertTrue(result.output.contains("[1.21-forge] gameTestServer runDir=${testServerDir}${Path.DIRECTORY_SEPARATOR}forge"))
        assertTrue(result.output.contains("[1.21-forge] server runDir=$runDir"))

        assertTrue(result.output.contains("[1.21-neoforge] client runDir=$runDir"))
        assertFalse(result.output.contains("[1.21-neoforge] datagen runDir=$runDir"))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestClient runDir=${testClientDir}${Path.DIRECTORY_SEPARATOR}neoforge"))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestServer runDir=${testServerDir}${Path.DIRECTORY_SEPARATOR}neoforge"))
        assertTrue(result.output.contains("[1.21-neoforge] server runDir=$runDir"))
    }

    @Test
    fun `test with a custom directories`() {
        gradleTest.setStonecutterVersion("1.20.2", "fabric", "forge")
        gradleTest.buildScript("""
            modSettings {
                runDirectory = rootProject.layout.projectDirectory.dir("run-test")
                testClientRunDirectory = rootProject.layout.projectDirectory.dir("run-test-client")
                testServerRunDirectory = rootProject.layout.projectDirectory.dir("run-test-server")    
            }
        """.trimIndent())

        val runDir = gradleTest.project().layout.projectDirectory.dir("versions/1.20.2-fabric").asFile.toPath().relativize(gradleTest.project().layout.projectDirectory.dir("run-test").asFile.toPath()).toString()
        val testClientDir = gradleTest.project().layout.projectDirectory.dir("versions/1.20.2-fabric").asFile.toPath().relativize(gradleTest.project().layout.projectDirectory.dir("run-test-client").asFile.toPath()).toString()
        val testServerDir = gradleTest.project().layout.projectDirectory.dir("versions/1.20.2-fabric").asFile.toPath().relativize(gradleTest.project().layout.projectDirectory.dir("run-test-server").asFile.toPath()).toString()

        val result = gradleTest.run("printLoomSettings")

        assertTrue(result.output.contains("[1.20.2-fabric] client runDir=$runDir"))
        assertTrue(result.output.contains("[1.20.2-fabric] datagen runDir=build/datagen"))
        assertTrue(result.output.contains("[1.20.2-fabric] gameTestClient runDir=$testClientDir"))
        assertTrue(result.output.contains("[1.20.2-fabric] gameTestServer runDir=$testServerDir"))
        assertTrue(result.output.contains("[1.20.2-fabric] server runDir=$runDir"))

        assertTrue(result.output.contains("[1.20.2-forge] client runDir=$runDir"))
        assertTrue(result.output.contains("[1.20.2-forge] datagen runDir=$runDir"))
        assertTrue(result.output.contains("[1.20.2-forge] gameTestClient runDir=$testClientDir"))
        assertTrue(result.output.contains("[1.20.2-forge] gameTestServer runDir=$testServerDir"))
        assertTrue(result.output.contains("[1.20.2-forge] server runDir=$runDir"))
    }

}
