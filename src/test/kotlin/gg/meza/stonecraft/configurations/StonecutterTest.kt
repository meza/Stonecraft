package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

/**
 * Since stonecutter.consts doesn't yet have a getter, the only way we can
 * test if our constants work is by using them e2e.
 *
 * This test will create a file in the resources directory and check if the
 * constants are correctly activated based on the active project.
 */
@DisplayName("Test stonecutter constants")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StonecutterTest : IntegrationTest {

    private lateinit var gradleTest: IntegrationTest.TestBuilder
    private lateinit var markerFile: File

    @BeforeEach
    fun setUp() {
        gradleTest = gradleTest()
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge", "neoforge")
        markerFile = gradleTest.project().layout.projectDirectory.file("src/main/resources/const.test.txt").asFile
    }

    @Test
    fun `when nothing has been actively selected`() {
        val contents = markerFile.readText()

        assertTrue(contents.contains("/*fabricline"), "Fabric line was activated incorrectly")
        assertTrue(contents.contains("/*fabriclikeline"), "Fabric like line was activated incorrectly")
        assertTrue(contents.contains("/*forgeline"), "Forge line was activated incorrectly")
        assertTrue(contents.contains("/*forgelikeline"), "Forge like line was activated incorrectly")
        assertTrue(contents.contains("/*neoforgeline"), "Neoforge line was activated incorrectly")
    }

    @Test
    fun `when fabric is selected`() {
        gradleTest.run("Set active project to 1.21.4-fabric")
        val contents = markerFile.readText()

        assertFalse(contents.contains("/*fabricline"), "Fabric line was not activated")
        assertFalse(contents.contains("/*fabriclikeline"), "Fabric like line was not activated")

        assertTrue(contents.contains("fabricline"), "Fabric line was not activated")
        assertTrue(contents.contains("fabriclikeline"), "Fabric like line was not activated")

        assertTrue(contents.contains("/*forgeline"), "Forge line was activated incorrectly")
        assertTrue(contents.contains("/*forgelikeline"), "Forge like line was activated incorrectly")
        assertTrue(contents.contains("/*neoforgeline"), "Neoforge line was activated incorrectly")
    }

    @Test
    fun `when forge is selected`() {
        gradleTest.run("Set active project to 1.21.4-forge")
        val contents = markerFile.readText()

        assertTrue(contents.contains("/*fabricline"), "Fabric line was activated incorrectly")
        assertTrue(contents.contains("/*fabriclikeline"), "Fabric like line was activated incorrectly")

        assertFalse(contents.contains("/*forgeline"), "Forge line was not activated")
        assertFalse(contents.contains("/*forgelikeline"), "Forge like line was not activated")

        assertTrue(contents.contains("forgeline"), "Forge line was not activated")
        assertTrue(contents.contains("forgelikeline"), "Forge like line was not activated")

        assertTrue(contents.contains("/*neoforgeline"), "Neoforge line was activated incorrectly")
    }

    @Test
    fun `when neoforge is selected`() {
        gradleTest.run("Set active project to 1.21.4-neoforge")
        val contents = markerFile.readText()

        assertTrue(contents.contains("/*fabricline"), "Fabric line was activated incorrectly")
        assertTrue(contents.contains("/*fabriclikeline"), "Fabric like line was activated incorrectly")
        assertTrue(contents.contains("/*forgeline"), "Forge line was activated incorrectly")

        assertFalse(contents.contains("/*neoforgeline"), "Neoforge line was not activated")
        assertFalse(contents.contains("/*forgelikeline"), "Forge like line was not activated")

        assertTrue(contents.contains("neoforgeline"), "Neoforge line was not activated")
        assertTrue(contents.contains("forgelikeline"), "Forge like line was not activated")
    }
}
