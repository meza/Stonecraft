package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar

fun resolveJarTask(
    project: Project,
    stonecutter: StonecutterBuildExtension,
    minecraftVersion: String
): TaskProvider<out AbstractArchiveTask> = if (stonecutter.eval(minecraftVersion, ">1.21.11")) {
    project.tasks.named("jar", Jar::class.java)
} else {
    project.tasks.named("remapJar", RemapJarTask::class.java)
}
