package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import okio.Path
import org.gradle.testkit.runner.BuildResult
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Big test file in order to save test execution time
 * We run the loom print task once and run tests against the output
 */
@DisplayName("Test loom configures everything")
class LoomFullTest : IntegrationTest {
    private lateinit var gradleTest: IntegrationTest.TestBuilder
    private lateinit var result: BuildResult

    @BeforeEach
    fun setUp() {
        if (::result.isInitialized) {
            return
        }
        gradleTest = gradleTest()
            .buildScript(loomTask)
            .buildScript(
                """
                if (project.name == "26.1-neoforge") {
                    loom {
                        accessWidenerPath = rootProject.layout.projectDirectory.file("src/main/resources/examplemod.deobfuscated.accesswidener")
                    }
                }
                """.trimIndent()
            )
            .setStonecutterVersion("1.21", "fabric", "forge", "neoforge")
            .setStonecutterVersion("1.21.4", "fabric", "forge", "neoforge")
            .setStonecutterVersion("26.1", "neoforge")

        result = gradleTest.run("printLoomSettings")
        gradleTest.assertNoGradleFailures(result)
    }

    @Test
    fun `all versions and loaders are represented`() {
        assertTrue(result.output.contains("1.21-fabric"))
        assertTrue(result.output.contains("1.21-forge"))
        assertTrue(result.output.contains("1.21-neoforge"))
        assertTrue(result.output.contains("1.21.4-fabric"))
        assertTrue(result.output.contains("1.21.4-forge"))
        assertTrue(result.output.contains("1.21.4-neoforge"))
        assertTrue(result.output.contains("26.1-neoforge"))
    }

    @Test
    fun `default rundir is set for all targets`() {
        var runDir = gradleTest.project().layout.projectDirectory.dir("versions/1.21-fabric/run")
        val rootRunDir = gradleTest.project().layout.projectDirectory.dir("run")

        assertTrue(result.output.contains("[1.21-fabric] client runDirectory=$rootRunDir"), "Expected $runDir")
        assertTrue(result.output.contains("[1.21-fabric] datagen runDirectory=${runDir}${Path.DIRECTORY_SEPARATOR}datagen"))
        assertTrue(result.output.contains("[1.21-fabric] gameTestClient runDirectory=${rootRunDir.dir("testclient/fabric")}"))
        assertTrue(result.output.contains("[1.21-fabric] gameTestServer runDirectory=${rootRunDir.dir("testserver/fabric")}"))
        assertTrue(result.output.contains("[1.21-fabric] server runDirectory=$rootRunDir"))

        assertTrue(result.output.contains("[1.21-forge] client runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21-forge] datagen runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21-forge] gameTestClient runDirectory=${rootRunDir.dir("testclient/forge")}"))
        assertTrue(result.output.contains("[1.21-forge] gameTestServer runDirectory=${rootRunDir.dir("testserver/forge")}"))
        assertTrue(result.output.contains("[1.21-forge] server runDirectory=$rootRunDir"))

        assertTrue(result.output.contains("[1.21-neoforge] client runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21-neoforge] Datagen runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestClient runDirectory=${rootRunDir.dir("testclient/neoforge")}"))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestServer runDirectory=${rootRunDir.dir("testserver/neoforge")}"))
        assertTrue(result.output.contains("[1.21-neoforge] server runDirectory=$rootRunDir"))

        runDir = gradleTest.project().layout.projectDirectory.dir("versions/1.21.4-fabric/run")
        assertTrue(result.output.contains("[1.21.4-fabric] client runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21.4-fabric] datagen runDirectory=${runDir}${Path.DIRECTORY_SEPARATOR}datagen"))
        assertTrue(result.output.contains("[1.21.4-fabric] gameTestClient runDirectory=${rootRunDir.dir("testclient/fabric")}"))
        assertTrue(result.output.contains("[1.21.4-fabric] gameTestServer runDirectory=${rootRunDir.dir("testserver/fabric")}"))
        assertTrue(result.output.contains("[1.21.4-fabric] server runDirectory=$rootRunDir"))

        assertTrue(result.output.contains("[1.21.4-forge] client runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21.4-forge] server runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21.4-forge] datagen runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21.4-forge] gameTestClient runDirectory=${rootRunDir.dir("testclient/forge")}"))
        assertTrue(result.output.contains("[1.21.4-forge] gameTestServer runDirectory=${rootRunDir.dir("testserver/forge")}"))

        assertTrue(result.output.contains("[1.21.4-neoforge] client runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21.4-neoforge] ClientDatagen runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21.4-neoforge] ServerDatagen runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestClient runDirectory=${rootRunDir.dir("testclient/neoforge")}"))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer runDirectory=${rootRunDir.dir("testserver/neoforge")}"))
        assertTrue(result.output.contains("[1.21.4-neoforge] server runDirectory=$rootRunDir"))

        assertTrue(result.output.contains("[26.1-neoforge] client runDirectory=$rootRunDir"))
        assertTrue(result.output.contains("[26.1-neoforge] gameTestClient runDirectory=${rootRunDir.dir("testclient/neoforge")}"))
        assertTrue(result.output.contains("[26.1-neoforge] gameTestServer runDirectory=${rootRunDir.dir("testserver/neoforge")}"))
        assertTrue(result.output.contains("[26.1-neoforge] server runDirectory=$rootRunDir"))
    }

    @Test
    fun `datagen options are set for all targets`() {
        fun generatedDir(version: String, loader: String): String = gradleTest.project().layout.projectDirectory.dir("versions/$version-$loader/src/main/generated").asFile.absolutePath
        val existingDir = gradleTest.project().layout.projectDirectory.dir("src/main/resources").asFile.absolutePath
        assertTrue(result.output.contains("[1.21-fabric] datagen jvmArguments=\"-Dfabric-api.datagen\""))
        assertTrue(result.output.contains("[1.21-fabric] datagen jvmArguments=\"-Dfabric-api.datagen.output-dir=${generatedDir("1.21", "fabric")}\""))
        assertTrue(result.output.contains("[1.21.4-fabric] datagen jvmArguments=\"-Dfabric-api.datagen\""))
        assertTrue(result.output.contains("[1.21.4-fabric] datagen jvmArguments=\"-Dfabric-api.datagen.output-dir=${generatedDir("1.21.4", "fabric")}\""))

        assertTrue(result.output.contains("[1.21-forge] datagen programArguments=\"--all\""))
        assertTrue(result.output.contains("[1.21-forge] datagen programArguments=\"--mod\""))
        assertTrue(result.output.contains("[1.21-forge] datagen programArguments=\"examplemod\""))
        assertTrue(result.output.contains("[1.21-forge] datagen programArguments=\"--output\""))
        assertTrue(result.output.contains("[1.21-forge] datagen programArguments=\"${generatedDir("1.21", "forge")}\""))
        assertTrue(result.output.contains("[1.21-forge] datagen programArguments=\"--existing\""))
        assertTrue(result.output.contains("[1.21-forge] datagen programArguments=\"${existingDir}\""))
        assertTrue(result.output.contains("[1.21-forge] datagen jvmArguments=\"-Dforge.logging.console.level=debug\""))
        assertTrue(result.output.contains("[1.21-forge] datagen jvmArguments=\"-Dforge.logging.markers=REGISTRIES\""))

        // This needs to be assertTrue once arch-loom fixes itself for modern forge
        assertTrue(result.output.contains("[1.21.4-forge] datagen programArguments=\"--all\""))
        assertTrue(result.output.contains("[1.21.4-forge] datagen programArguments=\"--mod\""))
        assertTrue(result.output.contains("[1.21.4-forge] datagen programArguments=\"examplemod\""))
        assertTrue(result.output.contains("[1.21.4-forge] datagen programArguments=\"--output\""))
        assertTrue(result.output.contains("[1.21.4-forge] datagen programArguments=\"${generatedDir("1.21.4", "forge")}\""))
        assertTrue(result.output.contains("[1.21.4-forge] datagen programArguments=\"--existing\""))
        assertTrue(result.output.contains("[1.21.4-forge] datagen programArguments=\"${existingDir}\""))
        assertTrue(result.output.contains("[1.21.4-forge] datagen jvmArguments=\"-Dforge.logging.console.level=debug\""))
        assertTrue(result.output.contains("[1.21.4-forge] datagen jvmArguments=\"-Dforge.logging.markers=REGISTRIES\""))

        assertTrue(result.output.contains("[1.21-neoforge] Datagen programArguments=\"--all\""))
        assertTrue(result.output.contains("[1.21-neoforge] Datagen programArguments=\"--mod\""))
        assertTrue(result.output.contains("[1.21-neoforge] Datagen programArguments=\"examplemod\""))
        assertTrue(result.output.contains("[1.21-neoforge] Datagen programArguments=\"--output\""))
        assertTrue(result.output.contains("[1.21-neoforge] Datagen programArguments=\"${generatedDir("1.21", "neoforge")}\""))
        assertTrue(result.output.contains("[1.21-neoforge] Datagen programArguments=\"--existing\""))
        assertTrue(result.output.contains("[1.21-neoforge] Datagen programArguments=\"${existingDir}\""))
        assertTrue(result.output.contains("[1.21-neoforge] Datagen jvmArguments=\"-Dneoforge.logging.console.level=debug\""))
        assertTrue(result.output.contains("[1.21-neoforge] Datagen jvmArguments=\"-Dneoforge.logging.markers=REGISTRIES\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] ClientDatagen"))
        assertTrue(result.output.contains("[26.1-neoforge] ClientDatagen"), "Modern Neoforge client datagen settings should exist")
        assertTrue(result.output.contains("[26.1-neoforge] ServerDatagen"), "Modern Neoforge server datagen settings should exist")
        assertTrue(result.output.contains("[26.1-neoforge] ClientDatagen programArguments=\"${generatedDir("26.1", "neoforge")}\""))
        assertTrue(result.output.contains("[26.1-neoforge] ServerDatagen programArguments=\"${generatedDir("26.1", "neoforge")}\""))
    }

    @Test
    fun `gameTestClient is properly set up`() {
        val fabricClient = listOf(
            "-Dfabric-api.gametest"
        )
        val forgeClient = listOf(
            "-Dforge.enabledGameTestNamespaces=examplemod",
            "-Dforge.enableGameTest=true"
        )
        val neoforgeClient = listOf(
            "-Dneoforge.enabledGameTestNamespaces=examplemod",
            "-Dneoforge.enableGameTest=true"
        )

        val fabric121 = gradleTest.project().layout.projectDirectory.file("versions/1.21-fabric/build/junit-client.xml").asFile.absolutePath
        val fabric1214 = gradleTest.project().layout.projectDirectory.file("versions/1.21.4-fabric/build/junit-client.xml").asFile.absolutePath

        assertTrue(result.output.contains("[1.21-fabric] gameTestClient jvmArguments=\"${fabricClient[0]}\""))
        assertTrue(result.output.contains("[1.21-fabric] gameTestClient jvmArguments=\"-Dfabric-api.gametest.report-file=${fabric121}\""))
        assertTrue(result.output.contains("[1.21.4-fabric] gameTestClient jvmArguments=\"${fabricClient[0]}\""))
        assertTrue(result.output.contains("[1.21.4-fabric] gameTestClient jvmArguments=\"-Dfabric-api.gametest.report-file=${fabric1214}\""))

        assertTrue(result.output.contains("[1.21.4-forge] gameTestClient"))
        assertTrue(result.output.contains("[1.21-forge] gameTestClient jvmArguments=\"${forgeClient[0]}\""))
        assertTrue(result.output.contains("[1.21-forge] gameTestClient jvmArguments=\"${forgeClient[1]}\""))

        assertTrue(result.output.contains("[1.21-neoforge] gameTestClient jvmArguments=\"${neoforgeClient[0]}\""))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestClient jvmArguments=\"${neoforgeClient[1]}\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestClient jvmArguments=\"${neoforgeClient[0]}\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestClient jvmArguments=\"${neoforgeClient[1]}\""))
        assertTrue(result.output.contains("[26.1-neoforge] gameTestClient jvmArguments=\"${neoforgeClient[0]}\""))
        assertTrue(result.output.contains("[26.1-neoforge] gameTestClient jvmArguments=\"${neoforgeClient[1]}\""))
    }

    @Test
    fun `gameTestServer is properly set up`() {
        val forgeServer = listOf(
            "-Dforge.enabledGameTestNamespaces=examplemod",
            "-Dforge.enableGameTest=true",
            "-Dforge.gameTestServer=true"
        )
        val neoforgeServer = listOf(
            "-Dneoforge.enabledGameTestNamespaces=examplemod",
            "-Dneoforge.enableGameTest=true",
            "-Dneoforge.gameTestServer=true"
        )

        assertTrue(result.output.contains("[1.21-fabric] gameTestServer jvmArguments=\"-Dfabric-api.gametest\""))
        assertTrue(result.output.contains("[1.21.4-fabric] gameTestServer jvmArguments=\"-Dfabric-api.gametest\""))

        assertTrue(result.output.contains("[1.21.4-forge] gameTestServer"))
        assertTrue(result.output.contains("[1.21-forge] gameTestServer jvmArguments=\"${forgeServer[0]}\""))
        assertTrue(result.output.contains("[1.21-forge] gameTestServer jvmArguments=\"${forgeServer[1]}\""))
        assertTrue(result.output.contains("[1.21-forge] gameTestServer jvmArguments=\"${forgeServer[2]}\""))

        assertTrue(result.output.contains("[1.21-neoforge] gameTestServer jvmArguments=\"${neoforgeServer[0]}\""))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestServer jvmArguments=\"${neoforgeServer[1]}\""))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestServer jvmArguments=\"${neoforgeServer[2]}\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer jvmArguments=\"${neoforgeServer[0]}\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer jvmArguments=\"${neoforgeServer[1]}\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer jvmArguments=\"${neoforgeServer[2]}\""))
        assertTrue(result.output.contains("[26.1-neoforge] gameTestServer mainClass=net.neoforged.fml.startup.GameTestServer"))
        assertTrue(result.output.contains("[26.1-neoforge] gameTestServer jvmArguments=\"${neoforgeServer[0]}\""))
        assertTrue(result.output.contains("[26.1-neoforge] gameTestServer jvmArguments=\"${neoforgeServer[1]}\""))
        assertTrue(result.output.contains("[26.1-neoforge] gameTestServer jvmArguments=\"${neoforgeServer[2]}\""))
    }

    companion object {
        @Language("gradle")
        var loomTask = """
    import gg.meza.stonecraft.mod
    import net.fabricmc.loom.configuration.ide.DefaultRunConfigurationSettings

    tasks.register("printLoomSettings") {
    val targetProject = project
    inputs.property("projectName", project.name)
    val projectName = inputs.properties["projectName"]
        doLast {
            println("[" + projectName + "] "+ "loom.accessWidenerPath=\"" + loom.accessWidenerPath.getOrNull() + "\"")
            if (mod.isForge) {
                println("[" + projectName + "] "+ "forge.convertAccessWideners=\"" + loom.forge.convertAccessWideners.getOrNull() + "\"")
            }

            loom.decompilerOptions.getByName("vineflower").options.get().forEach { (key, value) ->
                println("[" + projectName + "] "+ "decompilerOptions.vineflower.${'$'}key=\"${'$'}value\"")
            }
     
            loom.runConfigs.forEach{
                val runConfig = DefaultRunConfigurationSettings.finialise(it, targetProject)

                println("[" + projectName + "] "+ runConfig.name + " generateRunConfig="+runConfig.generateRunConfig.get())
                println("[" + projectName + "] "+ runConfig.name + " runtimeEnvironment="+runConfig.runtimeEnvironment.get())
                println("[" + projectName + "] "+ runConfig.name + " runDirectory="+runConfig.runDirectory.get())
                println("[" + projectName + "] "+ runConfig.name + " mainClass="+runConfig.mainClass.get())
                runConfig.programArguments.get().forEach { arg ->
                    println("[" + projectName + "] "+ it.name + " programArguments=\"" + arg + "\"")
                }
     
                runConfig.jvmArguments.get().forEach { arg ->
                    println("[" + projectName + "] "+ it.name + " jvmArguments=\"" + arg + "\"")
                }
            }
        }
    }     
        """.trimIndent()
    }
}
