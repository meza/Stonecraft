package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import okio.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@DisplayName("Test loom setup")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoomBitsTest : IntegrationTest {

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
        gradleTest.assertNoGradleFailures(br)

        assertFalse(br.output.contains("loom.accessWidenerPath"))
        assertFalse(br.output.contains("forge.convertAccessWideners"))
    }

    @Test
    fun `test access wideners when there is an access widener in the project`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge")
        val br = gradleTest.run("printLoomSettings")
        gradleTest.assertNoGradleFailures(br)
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
        gradleTest.buildScript(
            """
            modSettings {
                runDirectory = rootProject.layout.projectDirectory.dir("run-test")
            }
            """.trimIndent()
        )

        val result = gradleTest.run("printLoomSettings")
        gradleTest.assertNoGradleFailures(result)

        val runDir = gradleTest.project().rootProject.layout.projectDirectory.dir("run-test")
        val versionRunDir = gradleTest.project().layout.projectDirectory.dir("versions/1.21-fabric/run")

        assertTrue(result.output.contains("[1.21-fabric] client runDirectory=$runDir"))
        assertTrue(result.output.contains("[1.21-fabric] datagen runDirectory=${versionRunDir}${Path.DIRECTORY_SEPARATOR}datagen"))
        assertTrue(result.output.contains("[1.21-fabric] gameTestClient runDirectory=${runDir.dir("testclient")}"))
        assertTrue(result.output.contains("[1.21-fabric] gameTestServer runDirectory=${runDir.dir("testserver")}"))
        assertTrue(result.output.contains("[1.21-fabric] server runDirectory=$runDir"))

//        versionRunDir = gradleTest.project().layout.projectDirectory.dir("versions/1.21-forge/run")
        assertTrue(result.output.contains("[1.21-forge] client runDirectory=$runDir"))
        assertTrue(result.output.contains("[1.21-forge] datagen runDirectory=$runDir"))
        assertTrue(result.output.contains("[1.21-forge] gameTestClient runDirectory=${runDir.dir("testclient")}"))
        assertTrue(result.output.contains("[1.21-forge] gameTestServer runDirectory=${runDir.dir("testserver")}"))
        assertTrue(result.output.contains("[1.21-forge] server runDirectory=$runDir"))

        assertTrue(result.output.contains("[1.21-neoforge] client runDirectory=$runDir"))
        assertTrue(result.output.contains("[1.21-neoforge] Datagen runDirectory=$runDir"))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestClient runDirectory=${runDir.dir("testclient")}"))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestServer runDirectory=${runDir.dir("testserver")}"))
        assertTrue(result.output.contains("[1.21-neoforge] server runDirectory=$runDir"))
    }

    @Test
    fun `test with custom directories`() {
        gradleTest.setStonecutterVersion("1.20.2", "fabric", "forge")
        gradleTest.buildScript(
            """
            modSettings {
                runDirectory = rootProject.layout.projectDirectory.dir("run-test")
                testClientRunDirectory = rootProject.layout.projectDirectory.dir("run-test-client")
                testServerRunDirectory = rootProject.layout.projectDirectory.dir("run-test-server")    
            }
            """.trimIndent()
        )

        val runDir = gradleTest.project().rootProject.layout.projectDirectory.dir("run-test")
        val testClientDir = gradleTest.project().rootProject.layout.projectDirectory.dir("run-test-client")
        val testServerDir = gradleTest.project().rootProject.layout.projectDirectory.dir("run-test-server")

        val versionRunDir = gradleTest.project().layout.projectDirectory.dir("versions/1.20.2-fabric/run")

        val result = gradleTest.run("printLoomSettings")
        gradleTest.assertNoGradleFailures(result)

        System.out.println(result.output)
        System.out.println("runDir: $runDir")

        assertTrue(result.output.contains("[1.20.2-fabric] client runDirectory=$runDir"))
        assertTrue(result.output.contains("[1.20.2-fabric] datagen runDirectory=${versionRunDir}${Path.DIRECTORY_SEPARATOR}datagen"))
        assertTrue(result.output.contains("[1.20.2-fabric] gameTestClient runDirectory=$testClientDir"))
        assertTrue(result.output.contains("[1.20.2-fabric] gameTestServer runDirectory=$testServerDir"))
        assertTrue(result.output.contains("[1.20.2-fabric] server runDirectory=$runDir"))

        assertTrue(result.output.contains("[1.20.2-forge] client runDirectory=$runDir"))
        assertTrue(result.output.contains("[1.20.2-forge] datagen runDirectory=$runDir"))
        assertTrue(result.output.contains("[1.20.2-forge] gameTestClient runDirectory=$testClientDir"))
        assertTrue(result.output.contains("[1.20.2-forge] gameTestServer runDirectory=$testServerDir"))
        assertTrue(result.output.contains("[1.20.2-forge] server runDirectory=$runDir"))
    }

    @Test
    fun `test junit report paths`() {
        gradleTest.setStonecutterVersion("1.20.2", "fabric", "forge")
        gradleTest.buildScript(
            """
            modSettings {
                fabricClientJunitReportLocation = project.layout.buildDirectory.file("fabric-client-junit-report.xml")
                fabricServerJunitReportLocation = project.layout.buildDirectory.file("fabric-server-junit-report.xml")    
            }
            """.trimIndent()
        )

        val result = gradleTest.run("printLoomSettings")
        gradleTest.assertNoGradleFailures(result)

        assertTrue(result.output.contains("jvmArguments=\"-Dfabric-api.gametest.report-file=${gradleTest.project().layout.projectDirectory.file("versions/1.20.2-fabric/build/fabric-client-junit-report.xml").asFile.absolutePath}"))
        assertTrue(result.output.contains("jvmArguments=\"-Dfabric-api.gametest.report-file=${gradleTest.project().layout.projectDirectory.file("versions/1.20.2-fabric/build/fabric-server-junit-report.xml").asFile.absolutePath}"))
    }

    @Test
    fun `modern neoforge game test server uses the game test server main class`() {
        gradleTest.setStonecutterVersion("26.1", "neoforge")
        gradleTest.buildScript(
            """
            loom {
                accessWidenerPath = rootProject.layout.projectDirectory.file("src/main/resources/examplemod.deobfuscated.accesswidener")
            }
            """.trimIndent()
        )

        val result = gradleTest.run("printLoomSettings")
        gradleTest.assertNoGradleFailures(result)

        assertTrue(
            result.output.contains(
                "[26.1-neoforge] gameTestServer mainClass=net.neoforged.fml.startup.GameTestServer"
            )
        )
        assertTrue(result.output.contains("[26.1-neoforge] gameTestServer jvmArguments=\"-Dneoforge.enabledGameTestNamespaces=examplemod\""))
        assertTrue(result.output.contains("[26.1-neoforge] gameTestServer jvmArguments=\"-Dneoforge.enableGameTest=true\""))
        assertTrue(result.output.contains("[26.1-neoforge] gameTestServer jvmArguments=\"-Dneoforge.gameTestServer=true\""))
    }

    @Test
    fun `forge game test server uses the gameTestServer environment and template`() {
        gradleTest.setStonecutterVersion("1.21", "forge")

        val result = gradleTest.run("printLoomSettings")
        gradleTest.assertNoGradleFailures(result)

        assertTrue(result.output.contains("[1.21-forge] gameTestServer runtimeEnvironment=gameTestServer"))
        assertTrue(result.output.contains("[1.21-forge] gameTestServer jvmArguments=\"-Dforge.gameTestServer=true\""))
    }

    @Test
    fun `modern neoforge datagen uses dataClient and dataServer environments and templates`() {
        gradleTest.setStonecutterVersion("26.1", "neoforge")
        gradleTest.buildScript(
            """
            loom {
                accessWidenerPath = rootProject.layout.projectDirectory.file("src/main/resources/examplemod.deobfuscated.accesswidener")
            }
            """.trimIndent()
        )

        val result = gradleTest.run("printLoomSettings")
        gradleTest.assertNoGradleFailures(result)

        assertTrue(result.output.contains("[26.1-neoforge] ClientDatagen runtimeEnvironment=dataClient"))
        assertTrue(result.output.contains("[26.1-neoforge] ServerDatagen runtimeEnvironment=dataServer"))
        assertTrue(result.output.contains("[26.1-neoforge] ClientDatagen mainClass=net.neoforged.fml.startup.DataClient"))
        assertTrue(result.output.contains("[26.1-neoforge] ServerDatagen mainClass=net.neoforged.fml.startup.DataServer"))
    }

    @Test
    fun `legacy neoforge game test server does not use the modern game test server main class`() {
        gradleTest.setStonecutterVersion("1.21.4", "neoforge")

        val result = gradleTest.run("printLoomSettings")
        gradleTest.assertNoGradleFailures(result)

        assertFalse(
            result.output.contains(
                "[1.21.4-neoforge] gameTestServer mainClass=net.neoforged.fml.startup.GameTestServer"
            )
        )
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer jvmArguments=\"-Dneoforge.enabledGameTestNamespaces=examplemod\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer jvmArguments=\"-Dneoforge.enableGameTest=true\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer jvmArguments=\"-Dneoforge.gameTestServer=true\""))
    }
}
