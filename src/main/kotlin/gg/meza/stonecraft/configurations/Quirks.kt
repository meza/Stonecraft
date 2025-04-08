package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuild
import gg.meza.stonecraft.mod
import okio.Path
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.named

fun patchAroundArchitecturyQuirks(project: Project, stonecutter: StonecutterBuild) {
    addForgeJOPTDependency(project)
    removeUnnecessaryLWJGLDependencies(project, stonecutter)
}

/**
 * Architectury loom is adding LWGJL natives but not the actual LWJGL dependency causing Forge to not boot
 * We are removing the natives from the classpath from the SERVER because it's not needed
 *
 * @see https://github.com/architectury/architectury-loom/issues/191#issuecomment-2030567486
 */
private fun removeUnnecessaryLWJGLDependencies(project: Project, stonecutter: StonecutterBuild) {
    project.afterEvaluate {
        tasks.named<JavaExec>("runServer") {
            classpath = classpath.filter { !it.toString().contains("${Path.DIRECTORY_SEPARATOR}org.lwjgl${Path.DIRECTORY_SEPARATOR}") }
        }

        if (mod.isForge && stonecutter.eval(stonecutter.current.version, "<=1.21.4")) {
            tasks.named<JavaExec>("runGameTestServer") {
                classpath = classpath.filter { !it.toString().contains("${Path.DIRECTORY_SEPARATOR}org.lwjgl${Path.DIRECTORY_SEPARATOR}") }
            }
        }
    }
}

/**
 * @see https://discord.com/channels/792699517631594506/792701725106634783/1272848116864909314
 */
private fun addForgeJOPTDependency(project: Project) {
    if (project.mod.isForge) {
        project.configurations.configureEach {
            resolutionStrategy.force("net.sf.jopt-simple:jopt-simple:5.+")
        }
    }
}
