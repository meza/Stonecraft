package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import gg.meza.stonecraft.mod
import org.gradle.api.Project

// Set the stonecutter constants for platform detection.
// This is useful for conditional dependencies in the code (not in the buildscript)
fun configureStonecutterConstants(project: Project, stonecutter: StonecutterBuildExtension) {
    stonecutter.constants.match(project.mod.loader, "fabric", "forge", "neoforge", "quilt")
    stonecutter.filters.include("**/*.md", "**/*.txt")

    stonecutter.constants.put("forgeLike", project.mod.isForgeLike)
    stonecutter.constants.put("fabricLike", project.mod.isFabricLike)
}
