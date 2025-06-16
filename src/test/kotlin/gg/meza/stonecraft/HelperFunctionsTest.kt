package gg.meza.stonecraft

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HelperFunctionsTest {
    @Test
    fun `returns lowest defined value for versions below minimum`() {
        assertEquals(18, getResourceVersionFor("1.20.1"))
        assertEquals(18, getResourceVersionFor("1.19"))
        assertEquals(18, getResourceVersionFor("0.0.1"))
    }

    @Test
    fun `returns correct value for exact matches`() {
        assertEquals(18, getResourceVersionFor("1.20.2"))
        assertEquals(34, getResourceVersionFor("1.21"))
        assertEquals(46, getResourceVersionFor("1.21.4"))
        assertEquals(55, getResourceVersionFor("1.21.5"))
        assertEquals(63, getResourceVersionFor("1.21.6"))
    }

    @Test
    fun `returns correct value for versions between defined versions`() {
        assertEquals(34, getResourceVersionFor("1.21.1"))
        assertEquals(46, getResourceVersionFor("1.21.4.1"))
        assertEquals(55, getResourceVersionFor("1.21.5.2"))
    }

    @Test
    fun `returns highest defined value for versions above maximum`() {
        assertEquals(63, getResourceVersionFor("1.21.7"))
        assertEquals(63, getResourceVersionFor("2.0.0"))
        assertEquals(63, getResourceVersionFor("99.99.99"))
    }
}
