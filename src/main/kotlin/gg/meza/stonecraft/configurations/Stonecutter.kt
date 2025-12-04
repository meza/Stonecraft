package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import gg.meza.stonecraft.mod
import org.gradle.api.Project

// Set the stonecutter constants for platform detection.
// This is useful for conditional dependencies in the code (not in the buildscript)
fun configureStonecutterConstants(project: Project, stonecutter: StonecutterBuildExtension) {
    stonecutter.constants.match(project.mod.loader, "fabric", "forge", "neoforge", "quilt")

    stonecutter.constants.put("forgeLike", project.mod.isForgeLike)
    stonecutter.constants.put("fabricLike", project.mod.isFabricLike)
}

fun configureStonecutterHandlers(controller: StonecutterControllerExtension) {
    controller.handlers {
        inherit("json5", "json", "mcmeta", "md", "txt")
        inherit("cfg", "properties")
    }
}
