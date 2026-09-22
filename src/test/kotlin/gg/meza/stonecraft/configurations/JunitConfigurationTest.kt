package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Test JUnit configuration")
class JunitConfigurationTest : IntegrationTest {
    @Test
    fun `default configuration provides platform and supported loader integrations`() {
        val gradleTest = gradleTest()
            .setStonecutterVersion("1.21", "fabric", "forge", "neoforge")
            .buildScript(printJunitConfiguration)

        val result = gradleTest.run("printJunitConfiguration")
        gradleTest.assertNoGradleFailures(result)

        listOf("fabric", "forge", "neoforge").forEach { loader ->
            val projectPath = ":1.21-$loader"
            assertTrue(result.output.contains("$projectPath.testImplementation=org.junit.jupiter:junit-jupiter:5.10.2"))
            assertTrue(
                result.output.contains(
                    "$projectPath.testRuntimeOnly=org.junit.platform:junit-platform-launcher:1.10.2"
                )
            )
            assertTrue(result.output.contains("$projectPath.testOptions=JUnitPlatformOptions_Decorated"))
        }

        assertTrue(
            result.output.contains(":1.21-fabric.testImplementation=net.fabricmc:fabric-loader-junit:0.16.9")
        )
        assertFalse(result.output.contains(":1.21-forge.testImplementation=net.fabricmc:fabric-loader-junit"))
        assertFalse(result.output.contains(":1.21-forge.testRuntimeCapability=net.neoforged"))
        assertTrue(
            result.output.contains(
                ":1.21-neoforge.testRuntimeCapability=net.neoforged:neoforge-moddev-test-fixtures"
            )
        )
        assertTrue(
            result.output.contains(
                ":1.21-neoforge.testJvmArg=--add-opens=java.base/java.lang.invoke=ALL-UNNAMED"
            )
        )
        assertTrue(result.output.contains(":1.21-neoforge.fml.modFolders=main%%"))
    }

    @Test
    fun `disabled configuration leaves consumer owned junit setup untouched`() {
        val gradleTest = gradleTest()
            .setStonecutterVersion("1.21", "neoforge")
            .buildScript(
                """
                modSettings {
                    enableJunit = false
                }

                dependencies {
                    testImplementation("example:consumer-junit:9.9.9")
                    testRuntimeOnly("example:consumer-launcher:9.9.9")
                }

                tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
                    useJUnit()
                    jvmArgs("-Xss2m")
                    systemProperty("consumer.property", "owned")
                }

                $printJunitConfiguration
                """.trimIndent()
            )

        val result = gradleTest.run("printJunitConfiguration")
        gradleTest.assertNoGradleFailures(result)
        val projectPath = ":1.21-neoforge"

        assertTrue(result.output.contains("$projectPath.testImplementation=example:consumer-junit:9.9.9"))
        assertTrue(result.output.contains("$projectPath.testRuntimeOnly=example:consumer-launcher:9.9.9"))
        assertTrue(result.output.contains("$projectPath.testOptions=JUnitOptions_Decorated"))
        assertTrue(result.output.contains("$projectPath.testJvmArg=-Xss2m"))
        assertTrue(result.output.contains("$projectPath.consumer.property=owned"))
        assertFalse(result.output.contains("org.junit.jupiter:junit-jupiter"))
        assertFalse(result.output.contains("org.junit.platform:junit-platform-launcher"))
        assertFalse(result.output.contains("neoforge-moddev-test-fixtures"))
        assertFalse(result.output.contains("java.base/java.lang.invoke=ALL-UNNAMED"))
        assertFalse(result.output.contains("$projectPath.fml.modFolders="))
    }

    private companion object {
        val printJunitConfiguration = """
            tasks.register("printJunitConfiguration") {
                doLast {
                    val projectPath = project.path
                    listOf("testImplementation", "testRuntimeOnly").forEach { configurationName ->
                        configurations.getByName(configurationName).allDependencies.forEach { dependency ->
                            println(projectPath + "." + configurationName + "=" + dependency)
                            if (dependency is org.gradle.api.artifacts.ModuleDependency) {
                                dependency.requestedCapabilities.forEach { capability ->
                                    println(
                                        projectPath + ".testRuntimeCapability=" +
                                            capability.group + ":" + capability.name
                                    )
                                }
                            }
                        }
                    }

                    tasks.withType(org.gradle.api.tasks.testing.Test::class.java).forEach { testTask ->
                        println(projectPath + ".testOptions=" + testTask.options.javaClass.simpleName)
                        testTask.jvmArgs.forEach { argument ->
                            println(projectPath + ".testJvmArg=" + argument)
                        }
                        testTask.systemProperties.forEach { (key, value) ->
                            println(projectPath + "." + key + "=" + value)
                        }
                    }
                }
            }
        """.trimIndent()
    }
}
