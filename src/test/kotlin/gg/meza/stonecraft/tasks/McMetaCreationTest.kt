package gg.meza.stonecraft.tasks

import com.google.gson.GsonBuilder
import gg.meza.stonecraft.IntegrationTest
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.util.UUID

@DisplayName("Test pack mcmeta generation for forge")
class McMetaCreationTest : IntegrationTest {

    private lateinit var project: Project
    private lateinit var description: String
    private lateinit var outputFile: File
    private lateinit var inputFile: File

    @BeforeEach
    fun setUp() {
        description = UUID.randomUUID().toString()
        project = gradleTest().project()
        project.setProperties(mapOf("mod.description" to description))
        outputFile = project.layout.buildDirectory.file("resources/main/pack.mcmeta").get().asFile
        outputFile.parentFile.mkdirs()

        inputFile = project.rootProject.file("src/main/resources/pack.mcmeta")

        if (outputFile.exists()) {
            outputFile.delete()
        }

        if (inputFile.exists()) {
            inputFile.delete()
        }
    }

    @Test
    fun `packfile can be generated for old versions`() {
        val mcMetaCreation = project.tasks.register<McMetaCreation>("mcMetaCreation") {
            resourcePackVersion.set(6)
        }.get()

        mcMetaCreation.generateMcMeta()

        assertTrue(outputFile.exists(), "The pack.mcmeta file should be created.")

        /**
         * With the pack format being 6, the supported_formats field should NOT be present in the pack.mcmeta file.
         */
        val expectedContent = PackMeta(
            Pack(
                pack_format = 6,
                description = description
            )
        )

        val gson = GsonBuilder().create()
        val packMeta = gson.fromJson(outputFile.readText(), PackMeta::class.java)

        assertEquals(expectedContent, packMeta, "The pack.mcmeta file should contain the expected content.")
    }

    @Test
    fun `packfile correctly generates for the first version supporting the new format`() {
        val mcMetaCreation = project.tasks.register<McMetaCreation>("mcMetaCreation") {
            resourcePackVersion.set(18)
        }.get()

        mcMetaCreation.generateMcMeta()

        assertTrue(outputFile.exists(), "The pack.mcmeta file should be created.")

        /**
         * With the pack format being 6, the supported_formats field should be present in the pack.mcmeta file.
         */
        val expectedContent = PackMeta(
            Pack(
                pack_format = 18,
                description = description,
                supported_formats = SupportedFormat(18)
            )
        )

        val gson = GsonBuilder().create()
        val packMeta = gson.fromJson(outputFile.readText(), PackMeta::class.java)

        assertEquals(expectedContent, packMeta, "The pack.mcmeta file should contain the expected content.")
    }

    @Test
    fun `packfile correctly generates for the new versions`() {
        val mcMetaCreation = project.tasks.register<McMetaCreation>("mcMetaCreation") {
            resourcePackVersion.set(21)
        }.get()

        mcMetaCreation.generateMcMeta()

        assertTrue(outputFile.exists(), "The pack.mcmeta file should be created.")

        /**
         * With the pack format being 6, the supported_formats field should be present in the pack.mcmeta file.
         */
        val expectedContent = PackMeta(
            Pack(
                pack_format = 21,
                description = description,
                supported_formats = SupportedFormat(21)
            )
        )

        val gson = GsonBuilder().create()
        val packMeta = gson.fromJson(outputFile.readText(), PackMeta::class.java)

        assertEquals(expectedContent, packMeta, "The pack.mcmeta file should contain the expected content.")
    }

    @Test
    fun `existing packfile is used instead of generating one when one exists`() {
        val packFileContents = UUID.randomUUID().toString()

        inputFile.writeText(packFileContents)

        val mcMetaCreation = project.tasks.register<McMetaCreation>("mcMetaCreation") {
            resourcePackVersion.set(21)
        }.get()

        mcMetaCreation.generateMcMeta()

        assertFalse(outputFile.exists(), "The pack.mcmeta file should not exist as the processResources will take care of it.")
    }
}
