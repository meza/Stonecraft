package gg.meza.stonecraft

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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
        assertThrows(IllegalArgumentException::class.java) { getResourceVersionFor("1.20.1") }
    }
}
