package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.skills.STONECRAFT_SKILL_DESTINATION
import gg.meza.stonecraft.skills.StonecraftSkill
import gg.meza.stonecraft.tasks.InstallStonecraftSkill
import gg.meza.stonecraft.tasks.ReportStonecraftGuidanceVersion
import gg.meza.stonecraft.tasks.ShowStonecraftGuidance
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

private const val STONECRAFT_AGENT_TASK_GROUP = "help"

fun configureAgentSkillTasks(project: Project) {
    val embeddedSkill = StonecraftSkill.embedded
    val installedSkill = project.layout.projectDirectory.file(STONECRAFT_SKILL_DESTINATION)

    val guidance = project.tasks.register<ShowStonecraftGuidance>("stonecraftGuidance") {
        group = STONECRAFT_AGENT_TASK_GROUP
        description = "Prints Stonecraft and Stonecutter guidance"
        skillContent.set(embeddedSkill.content)
        skillVersion.set(embeddedSkill.version)
    }

    val guidanceVersion = project.tasks.register<ReportStonecraftGuidanceVersion>("stonecraftGuidanceVersion") {
        group = STONECRAFT_AGENT_TASK_GROUP
        description = "Reports the bundled and installed guidance versions"
        bundledVersion.set(embeddedSkill.version)
        this.installedSkill.from(installedSkill)
    }

    val installSkill = project.tasks.register<InstallStonecraftSkill>("installStonecraftSkill") {
        group = STONECRAFT_AGENT_TASK_GROUP
        description = "Installs the Stonecraft skill without replacing an existing copy"
        skillContent.set(embeddedSkill.content)
        destinationFile.set(installedSkill)
        destinationDisplayPath.set(STONECRAFT_SKILL_DESTINATION)
        forceOverwrite.convention(false)
    }

    guidance.configure {
        mustRunAfter(installSkill)
    }
    guidanceVersion.configure {
        mustRunAfter(installSkill)
    }
}
