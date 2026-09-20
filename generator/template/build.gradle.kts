__STONECRAFT_#PUBLISHING__
import gg.meza.stonecraft.mod
__STONECRAFT_/PUBLISHING__

plugins {
    id("gg.meza.stonecraft")
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
            "minecraftVersionVirtual" to stonecutter.current.version,
            "neoforgeLogo" to
                when (stonecutter.current.parsed < "26.3") {
                    true -> "logoFile"
                    false -> "iconFile"
                },
        )
}

__STONECRAFT_#PUBLISHING__
publishMods {
    modrinth {
        if (mod.isFabric) requires("fabric-api")
        environment.set(CLIENT_OR_SERVER_PREFERS_BOTH)
    }

    curseforge {
        client = true
        server = true
        if (mod.isFabric) requires("fabric-api")
    }
}
__STONECRAFT_/PUBLISHING__
