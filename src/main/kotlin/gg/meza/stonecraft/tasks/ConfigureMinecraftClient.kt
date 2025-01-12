package gg.meza.stonecraft.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import kotlin.io.path.createFile

/** Configures the Minecraft client
 *
 * Set some basic options on the minecraft client
 */
abstract class ConfigureMinecraftClient : DefaultTask() {

    @get:Input
    val clientOptions = project.objects.mapProperty(String::class.java, String::class.java)

    @get:InputDirectory
    val runDirectory = project.objects.directoryProperty()

    @get:Internal
    internal val defaults = mapOf(
        "guiScale" to "3",
        "narrator" to "0",
        "darkMojangStudiosBackground" to "true",
        "lastServer" to "127.0.0.1",
        "joinedFirstServer" to "true"
    )

    init {
        group = "minecraft"
        description = "Configures the Minecraft client"
        runDirectory.convention(project.layout.projectDirectory.dir("run"))
    }

    @TaskAction
    fun run() {
        logger.debug("Configuring Minecraft options in {}...", runDirectory.get())
        val optionsFile = runDirectory.get().asFile.resolve("options.txt").toPath()

        if (!Files.exists(optionsFile)) {
            optionsFile.createFile()
        }

        val optionsMap = mutableMapOf<String, String>()

        val optionsFileActual = optionsFile.toFile()

        if (optionsFileActual.length() > 0) {
            optionsFileActual.readLines().forEach { line ->
                val (key, value) = line.split(":")
                optionsMap[key] = value
            }
        }

        clientOptions.get().forEach { (key, value) ->
            updateOption(optionsMap, key, value)
        }

        val mergedValues = defaults + optionsMap

        optionsFileActual.writeText(mergedValues.map { (key, value) -> "$key:$value" }.joinToString("\n"))
        logger.debug("Minecraft options have been configured in ${optionsFileActual.absolutePath}.")
    }

    private fun updateOption(optionsMap: MutableMap<String, String>, key: String, value: String) {
        optionsMap[key] = if (value.contains(" ")) "\"$value\"" else value
    }
}
