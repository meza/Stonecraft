package gg.meza.stonecraft.tasks

import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.math.BigDecimal
import java.nio.file.Files

data class PackMeta(
    val pack: Pack
)

data class Pack(
    val pack_format: BigDecimal,
    val description: String,
    val supported_formats: SupportedFormat? = null
)

data class SupportedFormat(
    val min_inclusive: BigDecimal
)

@DisableCachingByDefault(because = "Generation depends on project metadata and existing resource state")
abstract class McMetaCreation : DefaultTask() {

    companion object {
        private const val FILENAME = "pack.mcmeta"
        private const val OUTPUT_PACK_FILE_PATH = "generated/stonecraft/resources/$FILENAME"
    }

    /**
     * User-authored pack metadata. When present, it takes precedence over generated metadata.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourcePackFiles: ConfigurableFileCollection

    /**
     * The description embedded in generated pack metadata.
     */
    @get:Input
    abstract val packDescription: Property<String>

    /**
     * The output file path for the generated pack.mcmeta file
     * Defaults to build/generated/stonecraft/resources/pack.mcmeta
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * The resource pack version to use in the pack.mcmeta file
     */
    @get:Input
    abstract val resourcePackVersion: Property<BigDecimal>

    init {
        group = "mod"
        description = "Creates a pack.mcmeta file for the embedded resource pack"
        outputFile.convention(project.layout.buildDirectory.file(OUTPUT_PACK_FILE_PATH))
    }

    @TaskAction
    fun generateMcMeta() {
        val version = requireNotNull(resourcePackVersion.orNull) { "Resource pack version must be set with `resourcePackVersion`" }
        val newFormat = version >= BigDecimal.valueOf(18L)

        val outputFilePath = outputFile.get().asFile
        if (sourcePackFiles.files.any { it.isFile }) {
            logger.lifecycle("Pack file exists, there's no need to generate one.")
            Files.deleteIfExists(outputFilePath.toPath())
            return
        }

        logger.lifecycle("No pack.mcmeta found, generating one...")

        val packMcMeta = PackMeta(
            Pack(
                pack_format = version,
                description = packDescription.get(),
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
