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
        gradleTest.buildScript(
            """
import gg.meza.stonecraft.mod

tasks.register("debugProps") {
    doLast {
        println("=== DEBUG: All extra properties ===")
        project.extra.properties.forEach { (key, value) ->
            println("EXTRA PROP: ${'$'}key = ${'$'}value")
        }
        println("=== DEBUG: Testing specific properties ===")
        println("fabric_version exists: " + mod.hasProp("fabric_version"))
        println("yarn_mappings exists: " + mod.hasProp("yarn_mappings"))
        println("loader_version exists: " + mod.hasProp("loader_version"))
        println("non_existent_property exists: " + mod.hasProp("non_existent_property"))
        
        println("=== DEBUG: Checking project.extra.has directly ===")
        println("project.extra.has(fabric_version): " + project.extra.has("fabric_version"))
        println("project.extra.has(yarn_mappings): " + project.extra.has("yarn_mappings"))
        println("project.extra.has(loader_version): " + project.extra.has("loader_version"))
        
        if (project.extra.has("fabric_version")) {
            println("fabric_version value: " + project.extra["fabric_version"])
        }
        if (project.extra.has("yarn_mappings")) {
            println("yarn_mappings value: " + project.extra["yarn_mappings"])
        }
    }
}
            """.trimIndent()
        )
        
        gradleTest.setStonecutterVersion("1.21.4", "fabric")
        
        val buildResult = gradleTest.run("debugProps")
        
        println("=== BUILD OUTPUT ===")
        println(buildResult.output)
        
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
        val testBuilder = gradleTest().buildScript(
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
        } catch (Exception e) {
            println("Exception caught: " + e.message)
        }
    }
}
            """.trimIndent()
        )
        
        testBuilder.setStonecutterVersion("1.21.4", "fabric")
        
        val buildResult = testBuilder.run("testPropIntegration")
        
        assertTrue(buildResult.output.contains("hasProp existing: true"), "hasProp should return true for existing property")
        assertTrue(buildResult.output.contains("hasProp missing: false"), "hasProp should return false for missing property")
        assertTrue(buildResult.output.contains("prop value: 0.114.0+1.21.4"), "prop should return the value for existing property")
        assertTrue(buildResult.output.contains("prop with default: 0.114.0+1.21.4"), "prop with default should return the value for existing property")
        assertTrue(buildResult.output.contains("prop with default for missing: default_value"), "prop with default should return default for missing property")
        assertTrue(buildResult.output.contains("Exception caught: Property missing_property not found"), "prop without default should throw for missing property")
    }

    @Test
    fun `hasProp can be used for conditional dependency loading pattern`() {
        val testBuilder = gradleTest().buildScript(
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
        
        testBuilder.setStonecutterVersion("1.21.4", "fabric")
        
        val buildResult = testBuilder.run("testConditionalDeps")
        
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

    @Test
    fun `debug properties loading`() {
        val testBuilder = gradleTest().buildScript(
            """
import gg.meza.stonecraft.mod

tasks.register("debugProperties") {
    doLast {
        println("=== PROJECT INFO ===")
        println("Project name: " + project.name)
        println("Project path: " + project.path)
        println("Root project: " + project.rootProject.name)
        
        println("=== ALL EXTRA PROPERTIES ===")
        project.extra.properties.forEach { (key, value) ->
            println("EXTRA[${'$'}key] = '${'$'}value' (type: ${'$'}{value?.javaClass?.simpleName})")
        }
        
        println("=== ROOT PROJECT EXTRA PROPERTIES ===")
        project.rootProject.extra.properties.forEach { (key, value) ->
            println("ROOT_EXTRA[${'$'}key] = '${'$'}value' (type: ${'$'}{value?.javaClass?.simpleName})")
        }
        
        println("=== TESTING SPECIFIC KEYS ===")
        val testKeys = listOf("fabric_version", "yarn_mappings", "loader_version", "forge_version")
        testKeys.forEach { key ->
            println("Key '${'$'}key':")
            println("  project.extra.has('${'$'}key'): " + project.extra.has(key))
            println("  project.extra['${'$'}key']: " + project.extra.properties[key])
            println("  project.rootProject.extra.has('${'$'}key'): " + project.rootProject.extra.has(key))
            println("  project.rootProject.extra['${'$'}key']: " + project.rootProject.extra.properties[key])
            try {
                println("  mod.prop('${'$'}key'): " + mod.prop(key))
            } catch (e: Exception) {
                println("  mod.prop('${'$'}key') ERROR: " + e.message)
            }
            println("  mod.hasProp('${'$'}key'): " + mod.hasProp(key))
        }
    }
}
            """.trimIndent()
        )
        
        testBuilder.setStonecutterVersion("1.21.4", "fabric")
        
        val buildResult = testBuilder.run("debugProperties")
        
        println("=== BUILD OUTPUT ===")
        println(buildResult.output)
        
        // This test is just for debugging, so we don't assert anything
        assertTrue(true, "Debug test completed")
    }
}