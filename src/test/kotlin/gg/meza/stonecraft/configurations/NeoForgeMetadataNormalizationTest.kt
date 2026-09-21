package gg.meza.stonecraft.configurations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NeoForgeMetadataNormalizationTest {

    @Test
    fun `modern neoforge metadata uses icon properties`() {
        assertEquals(
            "iconFile = \"icon.png\"",
            normalizeNeoForgeMetadata("logoFile = \"icon.png\"", usesIconMetadata = true)
        )
        assertEquals(
            "iconBlur = false",
            normalizeNeoForgeMetadata("logoBlur = false", usesIconMetadata = true)
        )
    }

    @Test
    fun `older neoforge metadata uses logo properties`() {
        assertEquals(
            "logoFile = \"icon.png\"",
            normalizeNeoForgeMetadata("iconFile = \"icon.png\"", usesIconMetadata = false)
        )
        assertEquals(
            "logoBlur = false",
            normalizeNeoForgeMetadata("iconBlur = false", usesIconMetadata = false)
        )
    }

    @Test
    fun `unrelated metadata remains unchanged`() {
        assertEquals(
            "# logoFile = \"icon.png\"",
            normalizeNeoForgeMetadata("# logoFile = \"icon.png\"", usesIconMetadata = true)
        )
    }
}
