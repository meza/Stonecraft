package gg.meza.stonecraft

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class HelperFunctionsTest {
    @Test
    fun `returns correct value for versions in the mapping`() {
        // spot check a few versions rather than the whole file
        assertEquals(18, getResourceVersionFor("1.20.2"))
        assertEquals(55, getResourceVersionFor("1.21.5"))
        assertEquals(63, getResourceVersionFor("1.21.6"))
    }

    @Test
    fun `throws for unknown versions`() {
        assertThrows(IllegalArgumentException::class.java) { getResourceVersionFor("0.20.1") }
    }

    @Test
    fun `exposes decimal resource pack formats`() {
        val packFormat = getResourcePackFormat("25w33a")

        assertEquals(BigDecimal("65.2"), packFormat.toBigDecimal())
        assertFalse(packFormat.isWholeNumber())
    }

    @Test
    fun `exposes decimal datapack formats`() {
        val datapackFormat = getDatapackFormat("25w33a")

        assertEquals(BigDecimal("83.1"), datapackFormat.toBigDecimal())
        assertFalse(datapackFormat.isWholeNumber())
    }

    @Test
    fun `integral accessor fails for decimal pack formats`() {
        assertThrows(IllegalStateException::class.java) {
            getResourceVersionFor("25w33a")
        }
    }
}
