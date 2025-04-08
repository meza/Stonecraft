package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuild
import gg.meza.stonecraft.mod
import org.gradle.api.Project

// Set the stonecutter constants for platform detection.
// This is useful for conditional dependencies in the code (not in the buildscript)
fun configureStonecutterConstants(project: Project, stonecutter: StonecutterBuild) {
    stonecutter.consts(project.mod.loader, "fabric", "forge", "neoforge", "quilt")
    stonecutter.allowExtensions("txt", "md")
    stonecutter.consts(
        Pair("forgeLike", project.mod.isForgeLike),
        Pair("fabricLike", project.mod.isFabricLike),
    )
}
