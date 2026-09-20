package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ForgeResourceTaskOwnershipTest : IntegrationTest {

    @Test
    fun `forge processed resources track generated metadata and source precedence across builds`() {
        val initialDescription = "Initial generated description"
        val forgeProject = gradleTest()
            .buildScript(
                """
                modSettings {
                    variableReplacements = mapOf(
                        "custom1" to "customValue1",
                        "custom2" to "customValue2",
                        "custom3" to "customValue3"
                    )
                }

                dependencies {
                    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
                    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
                }

                tasks.withType<Test> {
                    useJUnitPlatform()
                }
                """.trimIndent()
            )
            .setStonecutterVersion("1.20.2", "forge")
            .withProperties(
                mapOf(
                    "mod.id" to "examplemod",
                    "mod.name" to "Test Example Mod",
                    "mod.description" to initialDescription,
                    "mod.group" to "net.example",
                    "mod.version" to "1.0",
                    "org.gradle.caching" to "false"
                )
            )

        val testSource = forgeProject.project().layout.projectDirectory
            .file("src/test/java/com/example/ResourceTaskOwnershipTest.java")
            .asFile
        forgeProject.project().layout.projectDirectory
            .file("src/main/java/com/example/ExampleMod.java")
            .asFile
            .delete()
        testSource.parentFile.mkdirs()
        testSource.writeText(
            """
            package com.example;

            import org.junit.jupiter.api.Test;

            class ResourceTaskOwnershipTest {
                @Test
                void resourcesAreAvailableToTests() {}
            }
            """.trimIndent()
        )

        val versionProject = forgeProject.project().layout.projectDirectory.dir("versions/1.20.2-forge")
        val processedPack = versionProject.file("build/resources/main/pack.mcmeta").asFile
        val generatedPack = versionProject.file("build/generated/stonecraft/resources/pack.mcmeta").asFile

        val initialBuild = forgeProject.run("buildAndCollect", cacheTask = false)

        forgeProject.assertNoGradleFailures(initialBuild)
        assertTrue(processedPack.readText().contains(initialDescription))
        assertTrue(generatedPack.isFile, "Forge should generate metadata when the source pack is absent")

        val updatedDescription = "Updated incremental description"
        forgeProject.setModProperty("mod.description", updatedDescription)
        val descriptionBuild = forgeProject.run("buildAndCollect", cacheTask = false)

        forgeProject.assertNoGradleFailures(descriptionBuild)
        assertTrue(processedPack.readText().contains(updatedDescription))
        assertFalse(
            processedPack.readText().contains(initialDescription),
            "Changing mod.description must invalidate generated and processed metadata"
        )

        val sourcePack = forgeProject.project().layout.projectDirectory.file("src/main/resources/pack.mcmeta").asFile
        val sourcePackContents = """
            {
              "pack": {
                "pack_format": 18,
                "description": "Source-owned metadata"
              }
            }
        """.trimIndent()
        sourcePack.writeText(sourcePackContents)
        val sourceOwnedBuild = forgeProject.run("buildAndCollect", cacheTask = false)

        forgeProject.assertNoGradleFailures(sourceOwnedBuild)
        assertEquals(
            sourcePackContents,
            processedPack.readText(),
            "A source pack added after generation must take ownership of the processed resource"
        )
        assertFalse(generatedPack.exists(), "Source-owned metadata must remove the stale generated output")
    }
}
