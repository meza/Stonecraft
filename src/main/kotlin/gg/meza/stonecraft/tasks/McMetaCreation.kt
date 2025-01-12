package gg.meza.stonecraft.tasks

import com.google.gson.GsonBuilder
import gg.meza.stonecraft.mod
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files

data class PackMeta(
    val pack: Pack
)

data class Pack(
    val pack_format: Int,
    val description: String,
    val supported_formats: SupportedFormat? = null
)

data class SupportedFormat(
    val min_inclusive: Int
)

abstract class McMetaCreation : DefaultTask() {

    companion object {
        private const val FILENAME = "pack.mcmeta"
        private const val INPUT_PACK_FILE_PATH = "src/main/resources/$FILENAME"
        private const val OUTPUT_PACK_FILE_PATH = "resources/main/$FILENAME"
    }

    private val inputPackFile = project.rootProject.file(INPUT_PACK_FILE_PATH)

    /**
     * The output file path for the generated pack.mcmeta file
     * Defaults to build/resources/main/pack.mcmeta
     */
    @OutputFile
    var outputFilePath = project.layout.buildDirectory.file(OUTPUT_PACK_FILE_PATH).get().asFile

    /**
     * The resource pack version to use in the pack.mcmeta file
     */
    @Input
    val resourcePackVersion = project.objects.property(Int::class.java)

    init {
        group = "mod"
        description = "Creates a pack.mcmeta file for the embedded resource pack"
    }

    @TaskAction
    fun generateMcMeta() {
        val version = requireNotNull(resourcePackVersion.get()) { "Resource pack version must be set with `resourcePackVersion`" }
        val newFormat = version >= 18

        if (inputPackFile.exists()) {
            logger.lifecycle("Pack file exists, there's no need to generate one.")
            return
        }

        logger.lifecycle("No pack.mcmeta found, generating one...")

        val packMcMeta = PackMeta(
            Pack(
                pack_format = version,
                description = project.mod.description,
                supported_formats = if (newFormat) SupportedFormat(version) else null
            )
        )
        val gson = GsonBuilder().setPrettyPrinting().create()
        val packFileData = gson.toJson(packMcMeta)

        if (!Files.exists(outputFilePath.toPath())) {
            Files.createDirectories(outputFilePath.parentFile.toPath())
        }

        Files.writeString(
            outputFilePath.toPath(),
            packFileData,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
        )
    }
}
