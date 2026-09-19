package gg.meza.stonecraft.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

internal enum class StonecraftSkillWriteResult {
    CREATED,
    PRESERVED_EXISTING,
}

internal fun writeStonecraftSkill(
    destination: Path,
    content: String,
    forceOverwrite: Boolean,
): StonecraftSkillWriteResult {
    destination.parent?.let(Files::createDirectories)

    if (forceOverwrite) {
        Files.writeString(
            destination,
            content,
            Charsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE,
        )
        return StonecraftSkillWriteResult.CREATED
    }

    return try {
        Files.writeString(
            destination,
            content,
            Charsets.UTF_8,
            StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE,
        )
        StonecraftSkillWriteResult.CREATED
    } catch (_: FileAlreadyExistsException) {
        StonecraftSkillWriteResult.PRESERVED_EXISTING
    }
}

@DisableCachingByDefault(because = "Installation is an explicit user action and is too cheap to cache")
abstract class InstallStonecraftSkill : DefaultTask() {

    @get:Input
    abstract val skillContent: Property<String>

    @get:OutputFile
    abstract val destinationFile: RegularFileProperty

    @get:Input
    abstract val destinationDisplayPath: Property<String>

    @get:Input
    @get:Option(
        option = "force-overwrite",
        description = "Replace an existing Stonecraft skill, discarding its current contents"
    )
    abstract val forceOverwrite: Property<Boolean>

    @TaskAction
    fun install() {
        val skillFile = destinationFile.get().asFile
        val result = writeStonecraftSkill(
            skillFile.toPath(),
            skillContent.get(),
            forceOverwrite.get(),
        )

        if (result == StonecraftSkillWriteResult.PRESERVED_EXISTING) {
            logger.quiet(
                "A Stonecraft skill already exists at ${destinationDisplayPath.get()}; left it unchanged. " +
                    "Run ./gradlew stonecraftGuidance to merge an update while preserving user changes, or rerun " +
                    "./gradlew installStonecraftSkill --force-overwrite to replace it."
            )
            return
        }

        logger.lifecycle("Installed the Stonecraft skill at ${destinationDisplayPath.get()}")
    }
}
