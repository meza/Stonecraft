package gg.meza.stonecraft

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("ModData Tests")
class ModDataTest {

    private lateinit var project: Project
    private lateinit var modData: ModData

    @BeforeEach
    fun setUp() {
        project = ProjectBuilder.builder().build()
        
        // Set up required mod properties
        project.setProperty("mod.id", "test-mod")
        project.setProperty("mod.name", "Test Mod")
        project.setProperty("mod.description", "A test mod")
        project.setProperty("mod.version", "1.0.0")
        project.setProperty("mod.group", "com.test")
        
        // Set project name to include loader
        project.name = "test-mod-fabric"
        
        modData = ModData(project)
    }

    @Test
    fun `hasProp returns true when property exists`() {
        project.extensions.extraProperties["test_property"] = "test_value"
        
        assertTrue(modData.hasProp("test_property"), "hasProp should return true for existing property")
    }

    @Test
    fun `hasProp returns false when property does not exist`() {
        assertFalse(modData.hasProp("non_existent_property"), "hasProp should return false for non-existent property")
    }

    @Test
    fun `hasProp returns true for property with null value`() {
        project.extensions.extraProperties["null_property"] = null
        
        assertTrue(modData.hasProp("null_property"), "hasProp should return true even for null-valued property")
    }

    @Test
    fun `hasProp returns true for property with empty string value`() {
        project.extensions.extraProperties["empty_property"] = ""
        
        assertTrue(modData.hasProp("empty_property"), "hasProp should return true for empty string property")
    }

    @Test
    fun `hasProp works with complex property names`() {
        project.extensions.extraProperties["dependency_name"] = "fabric-api"
        project.extensions.extraProperties["minecraft.version"] = "1.21.4"
        project.extensions.extraProperties["mod-loader_type"] = "fabric"
        
        assertTrue(modData.hasProp("dependency_name"), "hasProp should work with underscore property names")
        assertTrue(modData.hasProp("minecraft.version"), "hasProp should work with dot-separated property names")
        assertTrue(modData.hasProp("mod-loader_type"), "hasProp should work with mixed property names")
    }

    @Test
    fun `hasProp integrates well with existing prop functions`() {
        project.extensions.extraProperties["existing_prop"] = "value"
        
        assertTrue(modData.hasProp("existing_prop"), "hasProp should return true for existing property")
        assertEquals("value", modData.prop("existing_prop"), "prop should return the value for existing property")
        assertEquals("value", modData.prop("existing_prop", "default"), "prop with default should return the value for existing property")
        
        assertFalse(modData.hasProp("missing_prop"), "hasProp should return false for missing property")
        assertEquals("default", modData.prop("missing_prop", "default"), "prop with default should return default for missing property")
        
        // Test that prop without default throws for missing property
        assertThrows<IllegalArgumentException> {
            modData.prop("missing_prop")
        }
    }

    @Test
    fun `hasProp can be used for conditional dependency loading pattern`() {
        // Simulate the use case described in the issue
        project.extensions.extraProperties["fabric_api_version"] = "0.92.0"
        // Note: "sodium_version" is intentionally not set to simulate missing optional dependency
        
        // This is the pattern users would use in their build files
        val shouldLoadFabricApi = modData.hasProp("fabric_api_version")
        val shouldLoadSodium = modData.hasProp("sodium_version")
        
        assertTrue(shouldLoadFabricApi, "Should load fabric-api when version is available")
        assertFalse(shouldLoadSodium, "Should not load sodium when version is not available")
        
        // Additional validation that the values can be retrieved when available
        if (shouldLoadFabricApi) {
            assertEquals("0.92.0", modData.prop("fabric_api_version"))
        }
    }
}