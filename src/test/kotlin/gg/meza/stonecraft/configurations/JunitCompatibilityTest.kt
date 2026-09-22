package gg.meza.stonecraft.configurations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JunitCompatibilityTest {

    @Test
    fun `fabric loader junit starts at 0-14-15`() {
        assertEquals(
            JunitSupportMode.PLATFORM_ONLY,
            JunitCompatibility.resolve("fabric", "0.14.14")
        )
        assertEquals(
            JunitSupportMode.FABRIC_LOADER,
            JunitCompatibility.resolve("fabric", "0.14.15")
        )
        assertEquals(
            JunitSupportMode.FABRIC_LOADER,
            JunitCompatibility.resolve("fabric", "0.19.2+local")
        )
    }

    @Test
    fun `neoforge fixtures start at 20-6-122`() {
        assertEquals(
            JunitSupportMode.PLATFORM_ONLY,
            JunitCompatibility.resolve("neoforge", "20.4.251")
        )
        assertEquals(
            JunitSupportMode.PLATFORM_ONLY,
            JunitCompatibility.resolve("neoforge", "20.6.121")
        )
        assertEquals(
            JunitSupportMode.NEOFORGE_FML_FIXTURES,
            JunitCompatibility.resolve("neoforge", "20.6.122")
        )
        assertEquals(
            JunitSupportMode.NEOFORGE_FML_FIXTURES,
            JunitCompatibility.resolve("neoforge", "26.1.0.19-beta")
        )
    }

    @Test
    fun `forge remains platform only`() {
        listOf("48.1.0", "66.0.2").forEach { forgeVersion ->
            assertEquals(
                JunitSupportMode.PLATFORM_ONLY,
                JunitCompatibility.resolve("forge", forgeVersion)
            )
        }
    }

    @Test
    fun `missing loader versions remain platform only`() {
        assertEquals(JunitSupportMode.PLATFORM_ONLY, JunitCompatibility.resolve("fabric", null))
        assertEquals(JunitSupportMode.PLATFORM_ONLY, JunitCompatibility.resolve("neoforge", null))
    }
}
