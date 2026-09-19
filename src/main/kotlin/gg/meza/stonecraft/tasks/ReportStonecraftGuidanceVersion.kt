package gg.meza.stonecraft.tasks

import gg.meza.stonecraft.skills.StonecraftSkill
import gg.meza.stonecraft.skills.StonecraftSkillState
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "This task only reports guidance versions to the console")
abstract class ReportStonecraftGuidanceVersion : DefaultTask() {

    @get:Input
    abstract val bundledVersion: Property<Int>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val installedSkill: ConfigurableFileCollection

    @TaskAction
    fun reportVersion() {
        val expectedVersion = bundledVersion.get()
        val installedContent = installedSkill.singleFile
            .takeIf { it.isFile }
            ?.readText(Charsets.UTF_8)
        val installation = StonecraftSkill.classify(installedContent, expectedVersion)

        logger.quiet("Bundled Stonecraft guidance version: $expectedVersion")
        when (installation.state) {
            StonecraftSkillState.MISSING -> logger.quiet("Installed Stonecraft guidance version: not installed")
            StonecraftSkillState.OUTDATED -> logger.quiet(
                "Installed Stonecraft guidance version: " +
                    "${installation.version?.toString() ?: "missing or invalid"} (upgrade available)"
            )
            StonecraftSkillState.CURRENT -> logger.quiet(
                "Installed Stonecraft guidance version: ${installation.version} (current)"
            )
        }
    }
}
