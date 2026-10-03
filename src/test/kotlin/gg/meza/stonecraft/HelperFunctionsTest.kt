package gg.meza.stonecraft

import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.OffsetDateTime

class HelperFunctionsTest {
    @Test
    fun `known versions keep their bundled formats`() {
        assertEquals(18, getResourceVersionFor("1.20.2"))
        assertEquals(BigDecimal("18"), getDatapackFormat("1.20.2").toBigDecimal())
        assertEquals(55, getResourceVersionFor("1.21.5"))
        assertEquals(63, getResourceVersionFor("1.21.6"))
    }

    @Test
    fun `unknown versions use both formats from the most recently released bundled version`() {
        val stream = requireNotNull(javaClass.classLoader.getResourceAsStream("pack_versions.json")) {
            "pack_versions.json not found"
        }
        val manifest = stream.reader().use { JsonParser.parseReader(it).asJsonObject }
        val latestEntry = manifest.entrySet().maxBy {
            OffsetDateTime.parse(it.value.asJsonObject.get("releaseTime").asString).toInstant()
        }
        val latestFormats = latestEntry.value.asJsonObject

        listOf(
            "${latestEntry.key}-unknown-release",
            "${latestEntry.key}-unknown-snapshot",
            "${latestEntry.key}-unknown-weekly-snapshot"
        ).forEach { unknownVersion ->
            assertFalse(manifest.has(unknownVersion), "$unknownVersion must be absent from the bundled manifest")
            assertEquals(
                latestFormats.get("datapack").asBigDecimal.stripTrailingZeros(),
                getDatapackFormat(unknownVersion).toBigDecimal(),
                "datapack format for $unknownVersion"
            )
            assertEquals(
                latestFormats.get("resourcepack").asBigDecimal.stripTrailingZeros(),
                getResourcePackFormat(unknownVersion).toBigDecimal(),
                "resource pack format for $unknownVersion"
            )
        }
    }

    @Test
    fun `fallback follows release time and keeps formats from one row`() {
        val json = JsonParser.parseString(
            """
            {
              "greatest-formats": {
                "datapack": 999,
                "resourcepack": 998,
                "releaseTime": "2026-01-01T00:00:00+00:00"
              },
              "newest-release": {
                "datapack": 122,
                "resourcepack": 98,
                "releaseTime": "2026-03-01T00:00:00+00:00"
              },
              "last-entry": {
                "datapack": 1,
                "resourcepack": 2,
                "releaseTime": "2026-02-01T00:00:00+00:00"
              }
            }
            """.trimIndent()
        ).asJsonObject

        val fallback = json.toPackFormatLookup()["unknown-version"]

        assertEquals(BigDecimal("122"), fallback.datapack.toBigDecimal())
        assertEquals(BigDecimal("98"), fallback.resourcepack.toBigDecimal())
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
