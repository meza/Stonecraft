plugins {
    id("gg.meza.stonecraft")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

modSettings {
    clientOptions {
        fov = 90
        guiScale = 3
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }
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

val awFile =
    when {
        stonecutter.current.parsed >= "26.1" -> "stonecraft_testmod.accesswidener"
        else -> "stonecraft_testmod.old.accesswidener"
    }

loom {
    accessWidenerPath =
        rootProject.layout.projectDirectory
            .file("src/main/resources/$awFile")
            .asFile
}
