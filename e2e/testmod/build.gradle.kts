import gg.meza.stonecraft.mod
import org.gradle.api.tasks.testing.Test

plugins {
    id("gg.meza.stonecraft")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
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

// Example of overriding publishing settings
publishMods {
    modrinth {
        if (mod.isFabric) requires("fabric-api")
    }

    curseforge {
        client = true // Set as needed
        server = false // Set as needed
        if (mod.isFabric) requires("fabric-api")
    }
}
