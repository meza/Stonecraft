package gg.meza.stonecraft

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ModData Tests")
class ModDataTest : IntegrationTest {

    private lateinit var gradleTest: IntegrationTest.TestBuilder

    @BeforeEach
    fun setUp() {
        gradleTest = gradleTest().buildScript(
            """
import gg.meza.stonecraft.mod

tasks.register("testHasProp") {
    doLast {
        println("fabric_version exists: " + mod.hasProp("fabric_version"))
        println("forge_version exists: " + mod.hasProp("forge_version"))
        println("non_existent_property exists: " + mod.hasProp("non_existent_property"))
        println("yarn_mappings exists: " + mod.hasProp("yarn_mappings"))
        println("loader_version exists: " + mod.hasProp("loader_version"))
        
        if (mod.hasProp("fabric_version")) {
            println("fabric_version value: " + mod.prop("fabric_version"))
        }
        if (mod.hasProp("forge_version")) {
            println("forge_version value: " + mod.prop("forge_version"))
        }
        if (mod.hasProp("yarn_mappings")) {
            println("yarn_mappings value: " + mod.prop("yarn_mappings"))
        }
    }
}
            """.trimIndent()
        )
    }

    @Test
    fun `hasProp returns true when property exists in version file`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")

        val buildResult = gradleTest.run("testHasProp")

        assertTrue(buildResult.output.contains("fabric_version exists: true"), "hasProp should return true for fabric_version property from 1.21.4.properties")
        assertTrue(buildResult.output.contains("yarn_mappings exists: true"), "hasProp should return true for yarn_mappings property from 1.21.4.properties")
        assertTrue(buildResult.output.contains("loader_version exists: true"), "hasProp should return true for loader_version property from 1.21.4.properties")
    }

    @Test
    fun `hasProp returns false when property does not exist`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")

        val buildResult = gradleTest.run("testHasProp")

        assertFalse(buildResult.output.contains("non_existent_property exists: true"), "hasProp should return false for non-existent property")
        assertTrue(buildResult.output.contains("non_existent_property exists: false"), "hasProp should return false for non-existent property")
    }

    @Test
    fun `hasProp works with different minecraft versions`() {
        gradleTest.setStonecutterVersion("1.21", "fabric")

        val buildResult = gradleTest.run("testHasProp")

        assertTrue(buildResult.output.contains("fabric_version exists: true"), "hasProp should return true for fabric_version property from 1.21.properties")
        assertTrue(buildResult.output.contains("fabric_version value: 0.102.0+1.21"), "Should load fabric_version from 1.21.properties")
    }

    @Test
    fun `hasProp integrates well with existing prop functions`() {
        gradleTest = gradleTest().buildScript(
            """
import gg.meza.stonecraft.mod

tasks.register("testPropIntegration") {
    doLast {
        val existingProp = "fabric_version"
        val missingProp = "missing_property"
        
        println("hasProp existing: " + mod.hasProp(existingProp))
        println("hasProp missing: " + mod.hasProp(missingProp))
        
        if (mod.hasProp(existingProp)) {
            println("prop value: " + mod.prop(existingProp))
            println("prop with default: " + mod.prop(existingProp, "default"))
        }
        
        if (!mod.hasProp(missingProp)) {
            println("prop with default for missing: " + mod.prop(missingProp, "default_value"))
        }
        
        try {
            mod.prop(missingProp)
            println("ERROR: Should have thrown exception")
        } catch (e: Exception) {
            println("Exception caught: " + e.message)
        }
    }
}
            """.trimIndent()
        )

        gradleTest.setStonecutterVersion("1.21.4", "fabric")

        val buildResult = gradleTest.run("testPropIntegration")

        assertTrue(buildResult.output.contains("hasProp existing: true"), "hasProp should return true for existing property")
        assertTrue(buildResult.output.contains("hasProp missing: false"), "hasProp should return false for missing property")
        assertTrue(buildResult.output.contains("prop value: 0.114.0+1.21.4"), "prop should return the value for existing property")
        assertTrue(buildResult.output.contains("prop with default: 0.114.0+1.21.4"), "prop with default should return the value for existing property")
        assertTrue(buildResult.output.contains("prop with default for missing: default_value"), "prop with default should return default for missing property")
        assertTrue(buildResult.output.contains("Exception caught: Cannot get property 'missing_property' on extra properties extension as it does not exist"), "prop without default should throw for missing property")
    }

    @Test
    fun `hasProp can be used for conditional dependency loading pattern`() {
        gradleTest = gradleTest().buildScript(
            """
import gg.meza.stonecraft.mod

tasks.register("testConditionalDeps") {
    doLast {
        // This is the pattern users would use in their build files
        val shouldLoadFabricApi = mod.hasProp("fabric_version")
        val shouldLoadSodium = mod.hasProp("sodium_version") // This property doesn't exist in fixture files
        
        println("Should load fabric-api: " + shouldLoadFabricApi)
        println("Should load sodium: " + shouldLoadSodium)
        
        // Additional validation that the values can be retrieved when available
        if (shouldLoadFabricApi) {
            println("Fabric API version: " + mod.prop("fabric_version"))
        }
    }
}
            """.trimIndent()
        )

        gradleTest.setStonecutterVersion("1.21.4", "fabric")

        val buildResult = gradleTest.run("testConditionalDeps")

        assertTrue(buildResult.output.contains("Should load fabric-api: true"), "Should load fabric-api when version is available")
        assertTrue(buildResult.output.contains("Should load sodium: false"), "Should not load sodium when version is not available")
        assertTrue(buildResult.output.contains("Fabric API version: 0.114.0+1.21.4"), "Should retrieve fabric_version value when available")
    }

    @Test
    fun `hasProp works with forge properties`() {
        gradleTest.setStonecutterVersion("1.21.4", "forge")

        val buildResult = gradleTest.run("testHasProp")

        assertTrue(buildResult.output.contains("forge_version exists: true"), "hasProp should return true for forge_version property from 1.21.4.properties")
        assertTrue(buildResult.output.contains("forge_version value: 1.21.4-54.0.16"), "Should load forge_version from 1.21.4.properties")
    }
}
