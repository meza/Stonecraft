import kotlin.io.relativeTo

plugins {
    id("gg.meza.stonecraft")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val awFile =
    when {
        stonecutter.current.parsed >= "26.1" -> {
            project.rootProject.layout.projectDirectory
                .file(
                    "src/main/resources/stonecraft_testmod.accesswidener",
                )
        }

        else -> {
            project.rootProject.layout.projectDirectory
                .file(
                    "src/main/resources/stonecraft_testmod.old.accesswidener",
                )
        }
    }

modSettings {
    clientOptions {
        fov = 90
        guiScale = 3
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }
    accessWidenerLocation = awFile
    variableReplacements =
        mapOf(
            "gameTestPackage" to
                when {
                    stonecutter.current.parsed < "1.21.5" -> "gametests.legacy"
                    else -> "gametests"
                },
        )
}

stonecutter {
    replacements.string(stonecutter.current.parsed < "1.21.11") {
        replace("Identifier", "ResourceLocation")
        replace("ResourceLocationParameter", "ResourceLocationParameter")
    }
}
