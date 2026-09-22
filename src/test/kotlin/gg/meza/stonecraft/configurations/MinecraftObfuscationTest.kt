package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Test Minecraft obfuscation configuration")
class MinecraftObfuscationTest : IntegrationTest {

    @Test
    fun `mapped alias configures every obfuscation consumer from the resolved version`() {
        val gradleTest = obfuscationTest("600.21.11")

        val result = gradleTest.run("printObfuscationConfiguration")
        gradleTest.assertNoGradleFailures(result)

        assertTrue(result.output.contains("disableObfuscation=false"))
        assertTrue(result.output.contains("mappings.present=true"))
        assertTrue(result.output.contains("fabricLoader.configuration=modImplementation"))
        assertTrue(result.output.contains("remapJar.injectAccessWidener=true"))
        assertTrue(result.output.contains("jar.accessWidenerInput=false"))
        assertTrue(result.output.contains("buildAndCollect.dep=remapJar"))
        assertTrue(result.output.contains("publishing.dep=remapJar"))
        assertFalse(result.output.contains("buildAndCollect.dep=jar"))
        assertFalse(result.output.contains("publishing.dep=jar"))
    }

    @Test
    fun `unobfuscated alias configures every obfuscation consumer from the resolved version`() {
        val gradleTest = obfuscationTest("current")

        val result = gradleTest.run("printObfuscationConfiguration")
        gradleTest.assertNoGradleFailures(result)

        assertTrue(result.output.contains("disableObfuscation=true"))
        assertTrue(result.output.contains("mappings.present=false"))
        assertTrue(result.output.contains("fabricLoader.configuration=implementation"))
        assertTrue(result.output.contains("remapJar.injectAccessWidener=false"))
        assertTrue(result.output.contains("jar.accessWidenerInput=true"))
        assertTrue(result.output.contains("buildAndCollect.dep=jar"))
        assertTrue(result.output.contains("publishing.dep=jar"))
        assertFalse(result.output.contains("buildAndCollect.dep=remapJar"))
        assertFalse(result.output.contains("publishing.dep=remapJar"))
    }

    @Test
    fun `unobfuscated prerelease alias stays on the unobfuscated artifact path`() {
        val gradleTest = obfuscationTest("upcoming")

        val result = gradleTest.run("printObfuscationConfiguration")
        gradleTest.assertNoGradleFailures(result)

        assertTrue(result.output.contains("disableObfuscation=true"))
        assertTrue(result.output.contains("mappings.present=false"))
        assertTrue(result.output.contains("jar.accessWidenerInput=true"))
        assertTrue(result.output.contains("buildAndCollect.dep=jar"))
        assertTrue(result.output.contains("publishing.dep=jar"))
        assertFalse(result.output.contains("buildAndCollect.dep=remapJar"))
        assertFalse(result.output.contains("publishing.dep=remapJar"))
    }

    private fun obfuscationTest(version: String): IntegrationTest.TestBuilder {
        val accessWidenerFileName = if (version == "current" || version == "upcoming") {
            "examplemod.deobfuscated.accesswidener"
        } else {
            "examplemod.accesswidener"
        }

        return gradleTest()
            .setStonecutterVersion(version, "fabric")
            .buildScript(
            """
            loom {
                accessWidenerPath = rootProject.layout.projectDirectory.file("src/main/resources/$accessWidenerFileName")
            }

            tasks.register("printObfuscationConfiguration") {
                doLast {
                    val fabricLoaderConfigurations = listOf("implementation", "modImplementation")
                        .filter { configurationName ->
                            configurations.findByName(configurationName)?.allDependencies?.any { dependency ->
                                dependency.group == "net.fabricmc" && dependency.name == "fabric-loader"
                            } == true
                        }
                    val mappingsPresent = configurations.findByName("mappings")?.allDependencies?.isNotEmpty() == true
                    val jarTask = tasks.named<org.gradle.jvm.tasks.Jar>("jar").get()
                    val remapJarTask = tasks.findByName("remapJar") as? net.fabricmc.loom.task.RemapJarTask
                    val buildAndCollect = tasks.named("buildAndCollect").get()
                    val publishing = project.extensions.getByType<me.modmuss50.mpp.ModPublishExtension>()

                    println("disableObfuscation=" + project.findProperty("fabric.loom.disableObfuscation"))
                    println("mappings.present=" + mappingsPresent)
                    fabricLoaderConfigurations.forEach { println("fabricLoader.configuration=" + it) }
                    println("remapJar.injectAccessWidener=" + (remapJarTask?.injectAccessWidener?.getOrElse(false) ?: false))
                    println(
                        "jar.accessWidenerInput=" + jarTask.inputs.files.files.any {
                            it.name == "$accessWidenerFileName"
                        }
                    )
                    buildAndCollect.taskDependencies.getDependencies(buildAndCollect)
                        .forEach { println("buildAndCollect.dep=" + it.name) }
                    project.files(publishing.file).buildDependencies.getDependencies(this)
                        .forEach { println("publishing.dep=" + it.name) }
                }
            }
            """.trimIndent()
        )
    }
}
