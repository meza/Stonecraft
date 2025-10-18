package gg.meza.stonecraft.tasks

import gg.meza.stonecraft.IntegrationTest
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@DisplayName("Test minecraft client configuration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigureMinecraftClientTest : IntegrationTest {

    private lateinit var testBuilder: IntegrationTest.TestBuilder
    private lateinit var project: Project
    private lateinit var optionsFile: File

    @BeforeEach
    fun setUp() {
        testBuilder = gradleTest()
        project = testBuilder.project()

        optionsFile = project.rootProject.file("run/options.txt")
        optionsFile.parentFile.mkdirs()
    }

    @Test
    fun `minecraft options can be created`() {
        if (optionsFile.exists()) {
            optionsFile.delete()
        }

        val task = project.tasks.register<ConfigureMinecraftClient>("configureMinecraftClient") {
            clientOptions.set(
                mapOf(
                    "guiScale" to "2",
                    "fov" to "80",
                    "narrator" to "1",
                    "music" to "0.5",
                    "darkMojangStudiosBackground" to "0",
                    "someKey" to "someValue",
                    "joinedFirstServer" to "false"
                )
            )
        }.get()

        task.run()

        val outputFileContents = optionsFile.readText()
        assertTrue(optionsFile.exists(), "The options.txt file should be created.")
        assertTrue(outputFileContents.contains("guiScale:2"), "The guiScale option should be set to 2 but is not.")
        assertTrue(outputFileContents.contains("fov:80"), "The fov option should be set to 80 but is not.")
        assertTrue(outputFileContents.contains("narrator:1"), "The narrator option should be set to true but is not.")
        assertTrue(outputFileContents.contains("music:0.5"), "The music option should be set to 0.5 but is not.")
        assertTrue(
            outputFileContents.contains("joinedFirstServer:false"),
            "The joinedFirstServer option is set from the defaults and not what we provided"
        )

        assertTrue(
            outputFileContents.contains("darkMojangStudiosBackground:0"),
            "The darkMojangStudiosBackground option should be set to false but is not."
        )
        assertTrue(
            outputFileContents.contains("someKey:someValue"),
            "The additionalLines option should be set to someKey:someValue but is not."
        )
    }

    @Test
    fun `existing minecraft options are correctly merged`() {
        if (optionsFile.exists()) {
            optionsFile.delete()
        }

        optionsFile.writeText("guiScale:1\nfov:70\nnarrator:false\nmusic:0.0\ndark_background:true\nexistingKey:existingValue")

        val task = project.tasks.register<ConfigureMinecraftClient>("configureMinecraftClient") {
            clientOptions.set(
                mapOf(
                    "guiScale" to "2",
                    "narrator" to "0",
                    "music" to "0.5",
                    "darkMojangStudiosBackground" to "1",
                    "someKey" to "someValue"
                )
            )
        }.get()

        task.run()

        val outputFileContents = optionsFile.readText()
        assertTrue(optionsFile.exists(), "The options.txt file should be created.")
        assertTrue(outputFileContents.contains("guiScale:2"), "The guiScale option should be set to 2 but is not.")
        assertTrue(outputFileContents.contains("fov:70"), "The fov option should be the original but is not.")
        assertTrue(outputFileContents.contains("narrator:0"), "The narrator option should be set to true but is not.")
        assertTrue(outputFileContents.contains("music:0.5"), "The music option should be set to 0.5 but is not.")
        assertTrue(
            outputFileContents.contains("joinedFirstServer:true"),
            "The joinedFirstServer option is not set from the defaults"
        )
        assertTrue(
            outputFileContents.contains("darkMojangStudiosBackground:1"),
            "The darkMojangStudiosBackground option should be set to false but is not."
        )
        assertTrue(
            outputFileContents.contains("someKey:someValue"),
            "The additionalLines option should be set to someKey:someValue but is not."
        )
        assertTrue(
            outputFileContents.contains("existingKey:existingValue"),
            "Original settings should be there but are not."
        )
    }
}
