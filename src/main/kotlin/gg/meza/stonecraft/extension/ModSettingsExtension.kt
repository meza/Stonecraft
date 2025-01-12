package gg.meza.stonecraft.extension

import gg.meza.stonecraft.data.MinecraftClientOptions
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.io.File
import javax.inject.Inject

abstract class ModSettingsExtension @Inject constructor(
    val project: Project,
    val loader: String
) {
    @Optional
    @get:Input
    val variableReplacements = project.objects.mapProperty(String::class.java, Any::class.java)

    @Internal
    @get:InputDirectory
    internal val generatedResourcesProp = project.objects.directoryProperty()

    @Internal
    @get:InputDirectory
    internal val runDirectoryProp = project.objects.directoryProperty()

    @Internal
    @get:InputDirectory
    internal val testClientRunDirectoryProp = project.objects.directoryProperty()

    @Internal
    @get:InputDirectory
    internal val testServerRunDirectoryProp = project.objects.directoryProperty()

    @Internal
    @get:InputFile
    internal val fabricClientJunitReportLocationProp = project.objects.fileProperty()

    @Internal
    @get:InputFile
    internal val fabricServerJunitReportLocationProp = project.objects.fileProperty()

    @get:Nested
    abstract val clientOptions: MinecraftClientOptions

    init {
        runDirectoryProp.convention(project.rootProject.layout.projectDirectory.dir("run"))
        generatedResourcesProp.convention(project.layout.projectDirectory.dir("src/main/generated"))
        testClientRunDirectoryProp.convention(runDirectoryProp.dir("testclient/$loader"))
        testServerRunDirectoryProp.convention(runDirectoryProp.dir("testserver/$loader"))
        fabricClientJunitReportLocationProp.convention(project.layout.buildDirectory.file("junit.xml"))
        fabricServerJunitReportLocationProp.convention(project.layout.buildDirectory.file("junit.xml"))
        variableReplacements.convention(mapOf())
    }

    var runDirectory: Directory
        get() = runDirectoryProp.get()
        set(value) {
            runDirectoryProp.set(value)
            runDirectoryProp.disallowChanges()
        }

    var testClientRunDirectory: Directory
        get() = testClientRunDirectoryProp.get()
        set(value) {
            testClientRunDirectoryProp.set(value)
            testClientRunDirectoryProp.disallowChanges()
        }

    var testServerRunDirectory: Directory
        get() = testServerRunDirectoryProp.get()
        set(value) {
            testServerRunDirectoryProp.set(value)
            testServerRunDirectoryProp.disallowChanges()
        }

    var generatedResources: Directory
        get() = generatedResourcesProp.get()
        set(value) {
            generatedResourcesProp.set(value)
            generatedResourcesProp.disallowChanges()
        }

    var fabricClientJunitReportLocation: File
        get() = fabricClientJunitReportLocationProp.get().asFile
        set(value) {
            fabricClientJunitReportLocationProp.set(value)
            fabricClientJunitReportLocationProp.disallowChanges()
        }

    var fabricServerJunitReportLocation: File
        get() = fabricServerJunitReportLocationProp.get().asFile
        set(value) {
            fabricServerJunitReportLocationProp.set(value)
            fabricServerJunitReportLocationProp.disallowChanges()
        }

    fun clientOptions(configure: MinecraftClientOptions.() -> Unit) {
        clientOptions.configure()
    }
}
