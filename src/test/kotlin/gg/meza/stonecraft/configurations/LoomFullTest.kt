package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import okio.Path
import org.gradle.testkit.runner.BuildResult
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

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
            .setStonecutterVersion("1.21", "fabric",
//                "forge",
                "neoforge")
            .setStonecutterVersion("1.21.4", "fabric",
//                "forge",
                "neoforge")

        result = gradleTest.run("printLoomSettings")
    }

    @Test
    fun `all versions and loaders are represented`() {
        assertTrue(result.output.contains("1.21-fabric"))
//        assertTrue(result.output.contains("1.21-forge"))
        assertTrue(result.output.contains("1.21-neoforge"))
        assertTrue(result.output.contains("1.21.4-fabric"))
//        assertTrue(result.output.contains("1.21.4-forge"))
        assertTrue(result.output.contains("1.21.4-neoforge"))
    }

    @Test
    fun `default rundir is set for all targets`() {
        val runDir = gradleTest.project().layout.projectDirectory.dir("versions/1.20.2-fabric").asFile.toPath().relativize(gradleTest.project().layout.projectDirectory.dir("run").asFile.toPath())
        val testClientDir = gradleTest.project().layout.projectDirectory.dir("versions/1.20.2-fabric").asFile.toPath().relativize(gradleTest.project().layout.projectDirectory.dir("run/testclient").asFile.toPath())
        val testServerDir = gradleTest.project().layout.projectDirectory.dir("versions/1.20.2-fabric").asFile.toPath().relativize(gradleTest.project().layout.projectDirectory.dir("run/testserver").asFile.toPath())

        assertTrue(result.output.contains("[1.21-fabric] client runDir=$runDir"))
        assertTrue(result.output.contains("[1.21-fabric] datagen runDir=build/datagen"))
        assertTrue(result.output.contains("[1.21-fabric] gameTestClient runDir=${testClientDir}${Path.DIRECTORY_SEPARATOR}fabric"))
        assertTrue(result.output.contains("[1.21-fabric] gameTestServer runDir=${testServerDir}${Path.DIRECTORY_SEPARATOR}fabric"))
        assertTrue(result.output.contains("[1.21-fabric] server runDir=$runDir"))

//        assertTrue(result.output.contains("[1.21-forge] client runDir=$runDir"))
//        assertTrue(result.output.contains("[1.21-forge] datagen runDir=$runDir"))
//        assertTrue(result.output.contains("[1.21-forge] gameTestClient runDir=${testClientDir}${Path.DIRECTORY_SEPARATOR}forge"))
//        assertTrue(result.output.contains("[1.21-forge] gameTestServer runDir=${testServerDir}${Path.DIRECTORY_SEPARATOR}forge"))
//        assertTrue(result.output.contains("[1.21-forge] server runDir=$runDir"))

        assertTrue(result.output.contains("[1.21-neoforge] client runDir=$runDir"))
        assertFalse(result.output.contains("[1.21-neoforge] datagen runDir=$runDir"))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestClient runDir=${testClientDir}${Path.DIRECTORY_SEPARATOR}neoforge"))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestServer runDir=${testServerDir}${Path.DIRECTORY_SEPARATOR}neoforge"))
        assertTrue(result.output.contains("[1.21-neoforge] server runDir=$runDir"))

        assertTrue(result.output.contains("[1.21.4-fabric] client runDir=$runDir"))
        assertTrue(result.output.contains("[1.21.4-fabric] datagen runDir=build/datagen"))
        assertTrue(result.output.contains("[1.21.4-fabric] gameTestClient runDir=${testClientDir}${Path.DIRECTORY_SEPARATOR}fabric"))
        assertTrue(result.output.contains("[1.21.4-fabric] gameTestServer runDir=${testServerDir}${Path.DIRECTORY_SEPARATOR}fabric"))
        assertTrue(result.output.contains("[1.21.4-fabric] server runDir=$runDir"))

//        assertTrue(result.output.contains("[1.21.4-forge] client runDir=$runDir"))
//        assertTrue(result.output.contains("[1.21.4-forge] server runDir=$runDir"))
//        assertFalse(result.output.contains("[1.21.4-forge] datagen runDir=$runDir"))
//        assertTrue(result.output.contains("[1.21.4-forge] gameTestClient runDir=${testClientDir}${Path.DIRECTORY_SEPARATOR}forge"))
//        assertTrue(result.output.contains("[1.21.4-forge] gameTestServer runDir=${testServerDir}${Path.DIRECTORY_SEPARATOR}forge"))

        assertTrue(result.output.contains("[1.21.4-neoforge] client runDir=$runDir"))
        assertFalse(result.output.contains("[1.21.4-neoforge] datagen runDir=$runDir"))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestClient runDir=${testClientDir}${Path.DIRECTORY_SEPARATOR}neoforge"))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer runDir=${testServerDir}${Path.DIRECTORY_SEPARATOR}neoforge"))
        assertTrue(result.output.contains("[1.21.4-neoforge] server runDir=$runDir"))
    }

    @Test
    fun `datagen options are set for all targets`() {
        fun generatedDir(version: String, loader: String): String = gradleTest.project().layout.projectDirectory.dir("versions/$version-$loader/src/main/generated").asFile.absolutePath
        val existingDir = gradleTest.project().layout.projectDirectory.dir("src/main/resources").asFile.absolutePath

        assertTrue(result.output.contains("[1.21-fabric] datagen vmArgs=\"-Dfabric-api.datagen\""))
        assertTrue(result.output.contains("[1.21-fabric] datagen vmArgs=\"-Dfabric-api.datagen.output-dir=${generatedDir("1.21", "fabric")}\""))
        assertTrue(result.output.contains("[1.21.4-fabric] datagen vmArgs=\"-Dfabric-api.datagen\""))
        assertTrue(result.output.contains("[1.21.4-fabric] datagen vmArgs=\"-Dfabric-api.datagen.output-dir=${generatedDir("1.21.4", "fabric")}\""))

//        assertTrue(result.output.contains("[1.21-forge] datagen programArgs=\"--all\""))
//        assertTrue(result.output.contains("[1.21-forge] datagen programArgs=\"--mod\""))
//        assertTrue(result.output.contains("[1.21-forge] datagen programArgs=\"examplemod\""))
//        assertTrue(result.output.contains("[1.21-forge] datagen programArgs=\"--output\""))
//        assertTrue(result.output.contains("[1.21-forge] datagen programArgs=\"${generatedDir("1.21", "forge")}\""))
//        assertTrue(result.output.contains("[1.21-forge] datagen programArgs=\"--existing\""))
//        assertTrue(result.output.contains("[1.21-forge] datagen programArgs=\"${existingDir}\""))
//        assertTrue(result.output.contains("[1.21-forge] datagen vmArgs=\"-Dforge.logging.console.level=debug\""))
//        assertTrue(result.output.contains("[1.21-forge] datagen vmArgs=\"-Dforge.logging.markers=REGISTRIES\""))

        // This needs to be assertTrue once arch-loom fixes itself for modern forge
//        assertFalse(result.output.contains("[1.21.4-forge] datagen programArgs=\"--all\""))
//        assertFalse(result.output.contains("[1.21.4-forge] datagen programArgs=\"--mod\""))
//        assertFalse(result.output.contains("[1.21.4-forge] datagen programArgs=\"examplemod\""))
//        assertFalse(result.output.contains("[1.21.4-forge] datagen programArgs=\"--output\""))
//        assertFalse(result.output.contains("[1.21.4-forge] datagen programArgs=\"${generatedDir("1.21.4", "forge")}\""))
//        assertFalse(result.output.contains("[1.21.4-forge] datagen programArgs=\"--existing\""))
//        assertFalse(result.output.contains("[1.21.4-forge] datagen programArgs=\"${existingDir}\""))
//        assertFalse(result.output.contains("[1.21.4-forge] datagen vmArgs=\"-Dforge.logging.console.level=debug\""))
//        assertFalse(result.output.contains("[1.21.4-forge] datagen vmArgs=\"-Dforge.logging.markers=REGISTRIES\""))

        // This also needs to be true and like the above once arch-loom is fixed
        assertFalse(result.output.contains("[1.21-neoforge] datagen"), "Neoforge datagen settings exist even thought they shouldn't")
        assertFalse(result.output.contains("[1.21.4-neoforge] datagen"), "Neoforge datagen settings exist even thought they shouldn't")
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

        assertTrue(result.output.contains("[1.21-fabric] gameTestClient vmArgs=\"${fabricClient[0]}\""))
        assertTrue(result.output.contains("[1.21-fabric] gameTestClient vmArgs=\"-Dfabric-api.gametest.report-file=${fabric121}\""))
        assertTrue(result.output.contains("[1.21.4-fabric] gameTestClient vmArgs=\"${fabricClient[0]}\""))
        assertTrue(result.output.contains("[1.21.4-fabric] gameTestClient vmArgs=\"-Dfabric-api.gametest.report-file=${fabric1214}\""))

//        assertTrue(result.output.contains("[1.21.4-forge] gameTestClient"))
//        assertTrue(result.output.contains("[1.21-forge] gameTestClient vmArgs=\"${forgeClient[0]}\""))
//        assertTrue(result.output.contains("[1.21-forge] gameTestClient vmArgs=\"${forgeClient[1]}\""))

        assertTrue(result.output.contains("[1.21-neoforge] gameTestClient vmArgs=\"${neoforgeClient[0]}\""))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestClient vmArgs=\"${neoforgeClient[1]}\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestClient vmArgs=\"${neoforgeClient[0]}\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestClient vmArgs=\"${neoforgeClient[1]}\""))
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

        assertTrue(result.output.contains("[1.21-fabric] gameTestServer vmArgs=\"-Dfabric-api.gametest\""))
        assertTrue(result.output.contains("[1.21.4-fabric] gameTestServer vmArgs=\"-Dfabric-api.gametest\""))

//        assertTrue(result.output.contains("[1.21.4-forge] gameTestServer"))
//        assertTrue(result.output.contains("[1.21-forge] gameTestServer vmArgs=\"${forgeServer[0]}\""))
//        assertTrue(result.output.contains("[1.21-forge] gameTestServer vmArgs=\"${forgeServer[1]}\""))
//        assertTrue(result.output.contains("[1.21-forge] gameTestServer vmArgs=\"${forgeServer[2]}\""))

        assertTrue(result.output.contains("[1.21-neoforge] gameTestServer vmArgs=\"${neoforgeServer[0]}\""))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestServer vmArgs=\"${neoforgeServer[1]}\""))
        assertTrue(result.output.contains("[1.21-neoforge] gameTestServer vmArgs=\"${neoforgeServer[2]}\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer vmArgs=\"${neoforgeServer[0]}\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer vmArgs=\"${neoforgeServer[1]}\""))
        assertTrue(result.output.contains("[1.21.4-neoforge] gameTestServer vmArgs=\"${neoforgeServer[2]}\""))
    }

    companion object {
        @Language("gradle")
        var loomTask = """
    import gg.meza.stonecraft.mod
    tasks.register("printLoomSettings") {
    inputs.property("projectName", project.name)
    val projectName = inputs.properties["projectName"]
        doLast {
            if (loom.accessWidenerPath.isPresent) {
                println("[" + projectName + "] "+ "loom.accessWidenerPath=\"" + loom.accessWidenerPath.get() + "\"")
                if (mod.isForge) {
                    println("[" + projectName + "] "+ "forge.convertAccessWideners=\"" + loom.forge.convertAccessWideners.get() + "\"")
                }
            }
    
            loom.decompilerOptions.getByName("vineflower").options.get().forEach { (key, value) ->
                println("[" + projectName + "] "+ "decompilerOptions.vineflower.${'$'}key=\"${'$'}value\"")
            }
    
            loom.runConfigs.forEach{
                println("[" + projectName + "] "+ it.name + " isIdeConfigGenerated="+it.isIdeConfigGenerated)
                println("[" + projectName + "] "+ it.name + " runDir="+it.runDir)
                it.programArgs.forEach { arg ->
                    println("[" + projectName + "] "+ it.name + " programArgs=\"" + arg + "\"")
                }
    
                it.vmArgs.forEach { arg ->
                    println("[" + projectName + "] "+ it.name + " vmArgs=\"" + arg + "\"")
                }
            }
        }
    }     
        """.trimIndent()
    }
}
