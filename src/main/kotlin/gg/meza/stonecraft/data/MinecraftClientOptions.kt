package gg.meza.stonecraft.data

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import javax.inject.Inject

abstract class MinecraftClientOptions @Inject constructor(
    objects: ObjectFactory
) {
    @get:Input
    abstract val guiScale: Property<Int>

    @Internal
    @get:Input
    internal val fovValue = objects.property(Double::class.java)

    var fov: Int
        get() = ((fovValue.get() + 1) * 40 + 30).toInt()
        set(value) {
            fovValue.set(-1.0 + (value - 30) * (2.0 / 80.0))
        }

    @get:Input
    abstract val narrator: Property<Boolean>

    @get:Input
    abstract val musicVolume: Property<Double>

    @get:Input
    abstract val darkBackground: Property<Boolean>

    @get:Input
    val additionalLines = objects.mapProperty(String::class.java, String::class.java)
    var narratorValue: String
        get() = if (narrator.isPresent && narrator.get()) "1" else "0"
        set(value) {
            narrator.set(value == "1")
        }

    init {
        guiScale.convention(3)
        narrator.convention(false)
        musicVolume.convention(1.0)
        darkBackground.convention(false)
        fovValue.convention(0.0)
    }

    fun getOptions(): Map<String, String> = mapOf(
        "guiScale" to guiScale.get().toString(),
        "fov" to fovValue.get().toString(),
        "narrator" to narratorValue,
        "soundCategory_music" to musicVolume.get().toString(),
        "darkMojangStudiosBackground" to darkBackground.get().toString()
    ) + additionalLines.get()
}
