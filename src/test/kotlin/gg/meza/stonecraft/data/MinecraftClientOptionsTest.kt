package gg.meza.stonecraft.data

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MinecraftClientOptionsTest {
    private val project = ProjectBuilder.builder().build()
    private val options = project.objects.newInstance(MinecraftClientOptions::class.java)

    @Test
    fun `fov getter and setter are symmetric`() {
        options.fov = 80
        assertEquals(80, options.fov)
    }

    @Test
    fun `fov value converts to minecraft format`() {
        options.fov = 110
        assertEquals(1.0, options.fovValue.get())
        options.fovValue.set(-1.0)
        assertEquals(30, options.fov)
    }
}
