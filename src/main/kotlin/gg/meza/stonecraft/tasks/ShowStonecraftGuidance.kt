package gg.meza.stonecraft.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "This task only emits guidance to the console")
abstract class ShowStonecraftGuidance : DefaultTask() {

    @get:Input
    abstract val skillContent: Property<String>

    @get:Input
    abstract val skillVersion: Property<Int>

    @TaskAction
    fun showGuidance() {
        val currentVersion = skillVersion.get()
        logger.quiet(
            """
            If you have a `stonecraft` skill already, use that and stop here now unless you're updating or repairing the skill itself.

            Use the `metadata.version` field to check whether the installed skill needs an update. If it is lower than $currentVersion, missing, or invalid, update the installed skill from the bundled skill printed below. Preserve all user-authored changes during the update unless the user explicitly asks to discard them.

            If no `stonecraft` skill is installed, offer to install it with ./gradlew installStonecraftSkill.
            """.trimIndent()
        )
        logger.quiet(skillContent.get())
    }
}
