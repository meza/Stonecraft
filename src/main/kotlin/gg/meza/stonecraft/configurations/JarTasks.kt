package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.MinecraftObfuscation
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

fun resolveJarTask(
    project: Project,
    minecraftObfuscation: MinecraftObfuscation,
): TaskProvider<out Jar> = if (minecraftObfuscation == MinecraftObfuscation.UNOBFUSCATED) {
    project.tasks.named("jar", Jar::class.java)
} else {
    project.tasks.named("remapJar", RemapJarTask::class.java)
}
