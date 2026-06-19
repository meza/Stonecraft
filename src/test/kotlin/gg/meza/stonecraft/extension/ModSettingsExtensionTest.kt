package gg.meza.stonecraft.extension

import gg.meza.stonecraft.mod
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val MOD_ID = "testmod-id"
private const val MOD_NAME = "Test Mod"
private const val MOD_DESCRIPTION = "Test Mod description"
private const val MOD_VERSION = "1.2.3"
private const val MOD_GROUP = "gg.meza.test"

class ModSettingsExtensionTest {

    private val project = ProjectBuilder.builder()
        .withName("fabric")
        .build()
        .withMod()

    @Test
    fun `defaults are configured for all mod settings knobs`() {
        val settings = createSettings()

        assertMod()
        assertEquals(project.rootProject.layout.projectDirectory.dir("run"), settings.runDirectory)
        assertEquals(project.layout.projectDirectory.dir("src/main/generated"), settings.generatedResources)
        assertEquals(project.rootProject.layout.projectDirectory.dir("run/testclient/fabric"), settings.testClientRunDirectory)
        assertEquals(project.rootProject.layout.projectDirectory.dir("run/testserver/fabric"), settings.testServerRunDirectory)
        assertEquals(project.layout.buildDirectory.file("junit-client.xml").get(), settings.fabricClientJunitReportLocation.get())
        assertEquals(project.layout.buildDirectory.file("junit-server.xml").get(), settings.fabricServerJunitReportLocation.get())
        assertEquals(project.rootProject.layout.projectDirectory.file("src/main/resources/${MOD_ID}.accesswidener"), settings.accessWidenerLocation)
        assertEquals(true, settings.accessWidenerProcessing)
        assertEquals(emptyMap<String, Any>(), settings.variableReplacements.get())
        assertEquals(true, settings.gametestEntrypointCleanup)
        assertEquals(
            mapOf(
                "guiScale" to "3",
                "fov" to "0.0",
                "narrator" to "0",
                "soundCategory_music" to "1.0",
                "darkMojangStudiosBackground" to "false"
            ),
            settings.clientOptions.getOptions()
        )
    }

    @Test
    fun `all mod settings knobs can be overridden`() {
        val settings = createSettings()
        val customRunDirectory = project.layout.projectDirectory.dir("custom-run")
        val customTestClientRunDirectory = project.layout.projectDirectory.dir("custom-test-client")
        val customTestServerRunDirectory = project.layout.projectDirectory.dir("custom-test-server")
        val customGeneratedResources = project.layout.projectDirectory.dir("custom-generated")
        val customClientJunitReport = project.layout.buildDirectory.file("custom-client-report.xml")
        val customServerJunitReport = project.layout.buildDirectory.file("custom-server-report.xml")
        val customAccessWidener = project.layout.buildDirectory.file("/src/main/resources/custom.accesswidener").get()

        settings.runDirectory = customRunDirectory
        settings.testClientRunDirectory = customTestClientRunDirectory
        settings.testServerRunDirectory = customTestServerRunDirectory
        settings.generatedResources = customGeneratedResources
        settings.fabricClientJunitReportLocation = customClientJunitReport
        settings.fabricServerJunitReportLocation = customServerJunitReport
        settings.variableReplacements.set(mapOf("custom" to "replacement"))
        settings.gametestEntrypointCleanup = false
        settings.accessWidenerLocation = customAccessWidener
        settings.accessWidenerProcessing = false
        settings.clientOptions {
            guiScale.set(4)
            fov = 110
            narrator.set(true)
            musicVolume.set(0.25)
            darkBackground.set(true)
            additionalLines.set(mapOf("joinedFirstServer" to "false"))
        }

        assertEquals(customRunDirectory, settings.runDirectory)
        assertEquals(customGeneratedResources, settings.generatedResources)
        assertEquals(customTestClientRunDirectory, settings.testClientRunDirectory)
        assertEquals(customTestServerRunDirectory, settings.testServerRunDirectory)
        assertEquals(customClientJunitReport.get(), settings.fabricClientJunitReportLocation.get())
        assertEquals(customServerJunitReport.get(), settings.fabricServerJunitReportLocation.get())
        assertEquals(mapOf("custom" to "replacement"), settings.variableReplacements.get())
        assertEquals(customAccessWidener, settings.accessWidenerLocation)
        assertEquals(false, settings.accessWidenerProcessing)
        assertEquals(false, settings.gametestEntrypointCleanup)
        assertEquals(
            mapOf(
                "guiScale" to "4",
                "fov" to "1.0",
                "narrator" to "1",
                "soundCategory_music" to "0.25",
                "darkMojangStudiosBackground" to "true",
                "joinedFirstServer" to "false"
            ),
            settings.clientOptions.getOptions()
        )
    }

    private fun createSettings(): ModSettingsExtension = project.objects.newInstance(ModSettingsExtension::class.java, project, "fabric")

    private fun assertMod() {
        assertEquals(MOD_ID, project.mod.id)
        assertEquals(MOD_NAME, project.mod.name)
        assertEquals(MOD_DESCRIPTION, project.mod.description)
        assertEquals(MOD_VERSION, project.mod.version)
        assertEquals(MOD_GROUP, project.mod.group)
        assertEquals("fabric", project.mod.loader)
    }

    private fun Project.withMod(): Project {
        extensions.extraProperties["mod.id"] = MOD_ID
        extensions.extraProperties["mod.name"] = MOD_NAME
        extensions.extraProperties["mod.description"] = MOD_DESCRIPTION
        extensions.extraProperties["mod.version"] = MOD_VERSION
        extensions.extraProperties["mod.group"] = MOD_GROUP
        return this
    }
}
