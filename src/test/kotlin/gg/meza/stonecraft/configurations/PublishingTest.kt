package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junitpioneer.jupiter.SetEnvironmentVariable

@DisplayName("Test publishing setup")
class PublishingTest : IntegrationTest {

    private lateinit var gradleTest: IntegrationTest.TestBuilder

    @BeforeEach
    fun setUp() {
        gradleTest = gradleTest().buildScript(
            """
import me.modmuss50.mpp.ModPublishExtension
tasks.register("publishingSettings") {
    val pub = project.extensions.getByType(ModPublishExtension::class.java)

    println("changelog=\"" + pub.changelog.get().trimIndent()+"\"")
    println("file=" + pub.file.get())
    println("version=" + pub.version.get())
    println("type=" + pub.type.get())
    println("modLoaders=" + pub.modLoaders.get())
    println("displayName=" + pub.displayName.get())
    
    if (pub.platforms.findByName("modrinth") != null) {
        pub.modrinth {
            println("modrinth.accessToken=" + accessToken.get())
            println("modrinth.projectId=" + projectId.get())
            println("modrinth.minecraftVersions=" + minecraftVersions.get())
            println("modrinth.announcementTitle=" + announcementTitle.get())
        }
    }
    
    if (pub.platforms.findByName("curseforge") != null) {
        pub.curseforge {
            println("curseforge.projectSlug=" + projectSlug.get())
            println("curseforge.projectId=" + projectId.get())
            println("curseforge.accessToken=" + accessToken.get())
            println("curseforge.minecraftVersions=" + minecraftVersions.get())
            println("curseforge.announcementTitle=" + announcementTitle.get())
        }
    }

    println("dryRun=" + pub.dryRun.get())

}
            """.trimIndent()
        )
    }

    @Test
    fun `publishing tasks are configured`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge")

        val br = gradleTest.run("tasks")

        assertTrue(br.output.contains("chiseledPublishMods"), "chiseledPublishMods was not set up")
        assertTrue(br.output.contains("publishMods"), "publishMods was not set up")
        assertTrue(br.output.contains("publish"), "publish was not set up")
    }

    @Test
    fun `publish values are set correctly for a single loader and version`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")
        gradleTest.setModProperty("mod.version", "1.3.4")

        val br = gradleTest.run("publishingSettings")
        val expectedFile = gradleTest.project().layout.projectDirectory.file("versions/1.21.4-fabric/build/libs/examplemod-fabric-1.3.4+mc1.21.4.jar").asFile.absolutePath

        assertTrue(br.output.contains("changelog=\"test changes\""), "Changelog has not been set correctly")
        assertTrue(br.output.contains(expectedFile), "File path has not been set correctly. Expected: $expectedFile")
        assertTrue(br.output.contains("1.3.4+fabric-1.21.4"), "Publishing version has not been set correctly")
        assertTrue(br.output.contains("type=STABLE"), "Publishing type has not been set correctly")
        assertTrue(br.output.contains("modLoaders=[fabric]"), "Publishing mod loaders has not been set correctly for fabric")
        assertTrue(br.output.contains("displayName=1.3.4 for Fabric 1.21.4"), "Display name has not been set correctly for fabric")
        assertTrue(br.output.contains("dryRun=true"), "Dry run has been set incorrectly")
    }

    @Test
    fun `publish determines a beta version`() {
        gradleTest.setModProperty("mod.version", "1.3.4-beta.2")

        val br = gradleTest.run("publishingSettings")

        assertTrue(br.output.contains("type=BETA"), "Publishing type has not been set correctly")
    }

    @Test
    fun `publish determines a next version`() {
        gradleTest.setModProperty("mod.version", "1.3.4-beta.3")

        val br = gradleTest.run("publishingSettings")

        assertTrue(br.output.contains("type=BETA"), "Publishing type has not been set correctly")
    }

    @Test
    fun `publish determines an alpha version`() {
        gradleTest.setModProperty("mod.version", "1.3.4-alpha.2")

        val br = gradleTest.run("publishingSettings")

        assertTrue(br.output.contains("type=ALPHA"), "Publishing type has not been set correctly")
    }

    @SetEnvironmentVariable(key = "RELEASE_TYPE", value = "beta")
    @Test
    fun `publish determines an overridden beta version`() {
        gradleTest.setModProperty("mod.version", "1.3.4-alpha.2")

        val br = gradleTest.run("publishingSettings")

        assertTrue(br.output.contains("type=BETA"), "Publishing type has not been set correctly")
    }

    @SetEnvironmentVariable(key = "DO_PUBLISH", value = "anything-but-true")
    @Test
    fun `dry run can be turned off with the correct setting`() {
        val br = gradleTest.run("publishingSettings")
        assertTrue(br.output.contains("dryRun=false"), "Dry run has been set incorrectly")
    }

    @Test
    fun `publish values are correctly set for a multi-loader and multi-version project`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge", "neoforge")
        gradleTest.setStonecutterVersion("1.21", "fabric", "forge", "neoforge")
        gradleTest.setModProperty("mod.version", "5.6.7")

        val br = gradleTest.run("publishingSettings")
        val expectedFiles = listOf(
            gradleTest.project().layout.projectDirectory.file("versions/1.21.4-fabric/build/libs/examplemod-fabric-5.6.7+mc1.21.4.jar").asFile.absolutePath,
            gradleTest.project().layout.projectDirectory.file("versions/1.21.4-forge/build/libs/examplemod-forge-5.6.7+mc1.21.4.jar").asFile.absolutePath,
            gradleTest.project().layout.projectDirectory.file("versions/1.21.4-neoforge/build/libs/examplemod-neoforge-5.6.7+mc1.21.4.jar").asFile.absolutePath,
            gradleTest.project().layout.projectDirectory.file("versions/1.21-fabric/build/libs/examplemod-fabric-5.6.7+mc1.21.jar").asFile.absolutePath,
            gradleTest.project().layout.projectDirectory.file("versions/1.21-forge/build/libs/examplemod-forge-5.6.7+mc1.21.jar").asFile.absolutePath,
            gradleTest.project().layout.projectDirectory.file("versions/1.21-neoforge/build/libs/examplemod-neoforge-5.6.7+mc1.21.jar").asFile.absolutePath
        )

        expectedFiles.forEach { expectedFile ->
            assertTrue(br.output.contains(expectedFile), "File path has not been set correctly. Expected: $expectedFile")
        }

        assertTrue(br.output.contains("displayName=5.6.7 for Fabric 1.21.4"), "Display name has not been set correctly for fabric 1.21.4")
        assertTrue(br.output.contains("displayName=5.6.7 for Forge 1.21.4"), "Display name has not been set correctly for forge 1.21.4")
        assertTrue(br.output.contains("displayName=5.6.7 for Neoforge 1.21.4"), "Display name has not been set correctly for neoforge 1.21.4")

        assertTrue(br.output.contains("displayName=5.6.7 for Fabric 1.21"), "Display name has not been set correctly for fabric 1.21")
        assertTrue(br.output.contains("displayName=5.6.7 for Forge 1.21"), "Display name has not been set correctly for forge 1.21")
        assertTrue(br.output.contains("displayName=5.6.7 for Neoforge 1.21"), "Display name has not been set correctly for neoforge 1.21")

        assertTrue(br.output.contains("displayName=5.6.7 for Fabric 1.21.4"), "Display name has not been set correctly for fabric 1.21.4")
        assertTrue(br.output.contains("displayName=5.6.7 for Forge 1.21.4"), "Display name has not been set correctly for forge 1.21.4")
        assertTrue(br.output.contains("displayName=5.6.7 for Neoforge 1.21.4"), "Display name has not been set correctly for neoforge 1.21.4")
        assertTrue(br.output.contains("displayName=5.6.7 for Fabric 1.21"), "Display name has not been set correctly for fabric 1.21")
        assertTrue(br.output.contains("displayName=5.6.7 for Forge 1.21"), "Display name has not been set correctly for forge 1.21")
        assertTrue(br.output.contains("displayName=5.6.7 for Neoforge 1.21"), "Display name has not been set correctly for neoforge 1.21")
    }

    @Test
    fun `warnings are not shown in dry-run when the platform environment variables are not defined`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")

        val br = gradleTest.run("publishingSettings")

        assertFalse(br.output.contains("Essential Modrinth variables not found, skipping Modrinth publishing"))
        assertFalse(br.output.contains("If you want to use Modrinth, please set the MODRINTH_TOKEN and MODRINTH_ID environment variables"))
        assertFalse(br.output.contains("Essential CurseForge variables not found, skipping CurseForge publishing"))
        assertFalse(br.output.contains("If you want to use CurseForge, please set the CURSEFORGE_SLUG, CURSEFORGE_ID, and CURSEFORGE_TOKEN environment variables"))
    }

    @SetEnvironmentVariable(key = "DO_PUBLISH", value = "anything-but-true")
    @Test
    fun `appropriate warnings are shown when the platform environment variables are not defined in non-dry run mode`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")

        val br = gradleTest.run("publishingSettings")

        assertTrue(br.output.contains("Essential Modrinth variables not found, skipping Modrinth publishing"))
        assertTrue(br.output.contains("If you want to use Modrinth, please set the MODRINTH_TOKEN and MODRINTH_ID environment variables"))
        assertTrue(br.output.contains("Essential CurseForge variables not found, skipping CurseForge publishing"))
        assertTrue(br.output.contains("If you want to use CurseForge, please set the CURSEFORGE_SLUG, CURSEFORGE_ID, and CURSEFORGE_TOKEN environment variables"))
    }

    @SetEnvironmentVariable(key = "MODRINTH_TOKEN", value = "a-test-token-123")
    @SetEnvironmentVariable(key = "MODRINTH_ID", value = "an-even-better-test-id-456")
    @Test
    fun `modrinth publishing can be configured for a single project`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")
        gradleTest.setModProperty("mod.version", "0.1.2")

        val br = gradleTest.run("publishingSettings")

        assertTrue(br.output.contains("modrinth.accessToken=a-test-token-123"), "Modrinth access token has not been set correctly")
        assertTrue(br.output.contains("modrinth.projectId=an-even-better-test-id-456"), "Modrinth project ID has not been set correctly")
        assertTrue(br.output.contains("modrinth.minecraftVersions=[1.21.4]"), "Modrinth minecraft versions have not been set correctly")
        assertTrue(br.output.contains("modrinth.announcementTitle=Download 0.1.2+fabric-1.21.4 from Modrinth"), "Modrinth announcement title has not been set correctly")
    }

    @SetEnvironmentVariable(key = "MODRINTH_TOKEN", value = "a-test-token-123.123")
    @SetEnvironmentVariable(key = "MODRINTH_ID", value = "an-even-better-test-id-456.456")
    @Test
    fun `modrinth publishing can be configured for a multi-loader project`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge")
        gradleTest.setStonecutterVersion("1.21", "neoforge")
        gradleTest.setModProperty("mod.version", "3.4.5")

        val br = gradleTest.run("publishingSettings")

        val versionCount1 = Regex(Regex.escape("modrinth.minecraftVersions=[1.21.4]")).findAll(br.output).count()
        val versionCount2 = Regex(Regex.escape("modrinth.minecraftVersions=[1.21]")).findAll(br.output).count()
        val versionCount3 = Regex(Regex.escape("modrinth.accessToken=a-test-token-123.123")).findAll(br.output).count()
        val versionCount4 = Regex(Regex.escape("modrinth.projectId=an-even-better-test-id-456.456")).findAll(br.output).count()

        assertEquals(2, versionCount1, "Modrinth minecraft versions have not been set correctly for 1.21.4")
        assertEquals(1, versionCount2, "Modrinth minecraft versions have not been set correctly for 1.21")
        assertEquals(3, versionCount3, "Modrinth access token has not been set correctly for all versions")
        assertEquals(3, versionCount4, "Modrinth project ID has not been set correctly for all versions")

        assertTrue(br.output.contains("modrinth.announcementTitle=Download 3.4.5+fabric-1.21.4 from Modrinth"), "Modrinth announcement title has not been set correctly for 1.21.4 Fabric")
        assertTrue(br.output.contains("modrinth.announcementTitle=Download 3.4.5+forge-1.21.4 from Modrinth"), "Modrinth announcement title has not been set correctly for 1.21.4 Forge")
        assertTrue(br.output.contains("modrinth.announcementTitle=Download 3.4.5+neoforge-1.21 from Modrinth"), "Modrinth announcement title has not been set correctly for 1.21 Neoforge")
    }

    @SetEnvironmentVariable(key = "CURSEFORGE_SLUG", value = "a-test-slug-123")
    @SetEnvironmentVariable(key = "CURSEFORGE_ID", value = "an-even-better-test-id-456")
    @SetEnvironmentVariable(key = "CURSEFORGE_TOKEN", value = "a-test-token-123")
    @Test
    fun `curseforge publishing can be configured for a single project`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")
        gradleTest.setModProperty("mod.version", "0.1.2")

        val br = gradleTest.run("publishingSettings")

        assertTrue(br.output.contains("curseforge.projectSlug=a-test-slug-123"), "CurseForge project slug has not been set correctly")
        assertTrue(br.output.contains("curseforge.projectId=an-even-better-test-id-456"), "CurseForge project ID has not been set correctly")
        assertTrue(br.output.contains("curseforge.accessToken=a-test-token-123"), "CurseForge access token has not been set correctly")
        assertTrue(br.output.contains("curseforge.minecraftVersions=[1.21.4]"), "CurseForge minecraft versions have not been set correctly")
        assertTrue(br.output.contains("curseforge.announcementTitle=Download 0.1.2+fabric-1.21.4 from CurseForge"), "CurseForge announcement title has not been set correctly")
    }

    @SetEnvironmentVariable(key = "CURSEFORGE_SLUG", value = "a-test-slug-123.123")
    @SetEnvironmentVariable(key = "CURSEFORGE_ID", value = "an-even-better-test-id-456.456")
    @SetEnvironmentVariable(key = "CURSEFORGE_TOKEN", value = "a-test-token-123.123")
    @Test
    fun `curseforge publishing can be configured for a multi-loader project`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge")
        gradleTest.setStonecutterVersion("1.21", "neoforge")
        gradleTest.setModProperty("mod.version", "3.4.5")

        val br = gradleTest.run("publishingSettings")

        val versionCount1 = Regex(Regex.escape("curseforge.minecraftVersions=[1.21.4]")).findAll(br.output).count()
        val versionCount2 = Regex(Regex.escape("curseforge.minecraftVersions=[1.21]")).findAll(br.output).count()
        val versionCount3 = Regex(Regex.escape("curseforge.accessToken=a-test-token-123.123")).findAll(br.output).count()
        val versionCount4 = Regex(Regex.escape("curseforge.projectId=an-even-better-test-id-456.456")).findAll(br.output).count()

        assertEquals(2, versionCount1, "CurseForge minecraft versions have not been set correctly for 1.21.4")
        assertEquals(1, versionCount2, "CurseForge minecraft versions have not been set correctly for 1.21")
        assertEquals(3, versionCount3, "CurseForge access token has not been set correctly for all versions")
        assertEquals(3, versionCount4, "CurseForge project ID has not been set correctly for all versions")

        assertTrue(br.output.contains("curseforge.announcementTitle=Download 3.4.5+fabric-1.21.4 from CurseForge"), "CurseForge announcement title has not been set correctly for 1.21.4 Fabric")
        assertTrue(br.output.contains("curseforge.announcementTitle=Download 3.4.5+forge-1.21.4 from CurseForge"), "CurseForge announcement title has not been set correctly for 1.21.4 Forge")
        assertTrue(br.output.contains("curseforge.announcementTitle=Download 3.4.5+neoforge-1.21 from CurseForge"), "CurseForge announcement title has not been set correctly for 1.21 Neoforge")
    }

    @SetEnvironmentVariable(key = "MODRINTH_TOKEN", value = "a-test-modrinth-token-123")
    @SetEnvironmentVariable(key = "MODRINTH_ID", value = "an-even-better-test-modrinth-id-456")
    @SetEnvironmentVariable(key = "CURSEFORGE_SLUG", value = "a-test-curseforge-slug-123")
    @SetEnvironmentVariable(key = "CURSEFORGE_ID", value = "an-even-better-test-curseforge-id-456")
    @SetEnvironmentVariable(key = "CURSEFORGE_TOKEN", value = "a-test-curseforge-token-123")
    @Test
    fun `both modrinth and curseforge publishing can be configured for a single project`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric")
        gradleTest.setModProperty("mod.version", "0.1.2")

        val br = gradleTest.run("publishingSettings")

        assertTrue(br.output.contains("modrinth.accessToken=a-test-modrinth-token-123"), "Modrinth access token has not been set correctly")
        assertTrue(br.output.contains("modrinth.projectId=an-even-better-test-modrinth-id-456"), "Modrinth project ID has not been set correctly")
        assertTrue(br.output.contains("modrinth.minecraftVersions=[1.21.4]"), "Modrinth minecraft versions have not been set correctly")
        assertTrue(br.output.contains("modrinth.announcementTitle=Download 0.1.2+fabric-1.21.4 from Modrinth"), "Modrinth announcement title has not been set correctly")

        assertTrue(br.output.contains("curseforge.projectSlug=a-test-curseforge-slug-123"), "CurseForge project slug has not been set correctly")
        assertTrue(br.output.contains("curseforge.projectId=an-even-better-test-curseforge-id-456"), "CurseForge project ID has not been set correctly")
        assertTrue(br.output.contains("curseforge.accessToken=a-test-curseforge-token-123"), "CurseForge access token has not been set correctly")
        assertTrue(br.output.contains("curseforge.minecraftVersions=[1.21.4]"), "CurseForge minecraft versions have not been set correctly")
        assertTrue(br.output.contains("curseforge.announcementTitle=Download 0.1.2+fabric-1.21.4 from CurseForge"), "CurseForge announcement title has not been set correctly")
    }

    @SetEnvironmentVariable(key = "MODRINTH_TOKEN", value = "a-test-modrinth-token-123.123")
    @SetEnvironmentVariable(key = "MODRINTH_ID", value = "an-even-better-test-modrinth-id-456.456")
    @SetEnvironmentVariable(key = "CURSEFORGE_SLUG", value = "a-test-curseforge-slug-123.123")
    @SetEnvironmentVariable(key = "CURSEFORGE_ID", value = "an-even-better-test-curseforge-id-456.456")
    @SetEnvironmentVariable(key = "CURSEFORGE_TOKEN", value = "a-test-curseforge-token-123.123")
    @Test
    fun `both modrinth and curseforge publishing can be configured for a multi-loader project`() {
        gradleTest.setStonecutterVersion("1.21.4", "fabric", "forge")
        gradleTest.setStonecutterVersion("1.21", "neoforge")
        gradleTest.setModProperty("mod.version", "3.4.5")

        val br = gradleTest.run("publishingSettings")

        val versionCount1 = Regex(Regex.escape("modrinth.minecraftVersions=[1.21.4]")).findAll(br.output).count()
        val versionCount2 = Regex(Regex.escape("modrinth.minecraftVersions=[1.21]")).findAll(br.output).count()
        val versionCount3 =
            Regex(Regex.escape("modrinth.accessToken=a-test-modrinth-token-123.123")).findAll(br.output).count()
        val versionCount4 =
            Regex(Regex.escape("modrinth.projectId=an-even-better-test-modrinth-id-456.456")).findAll(br.output).count()

        assertEquals(2, versionCount1, "Modrinth minecraft versions have not been set correctly for 1.21.4")
        assertEquals(1, versionCount2, "Modrinth minecraft versions have not been set correctly for 1.21")
        assertEquals(3, versionCount3, "Modrinth access token has not been set correctly for all versions")
        assertEquals(3, versionCount4, "Modrinth project ID has not been set correctly for all versions")

        assertTrue(
            br.output.contains("modrinth.announcementTitle=Download 3.4.5+fabric-1.21.4 from Modrinth"),
            "Modrinth announcement title has not been set correctly for 1.21.4 Fabric"
        )
        assertTrue(
            br.output.contains("modrinth.announcementTitle=Download 3.4.5+forge-1.21.4 from Modrinth"),
            "Modrinth announcement title has not been set correctly for 1.21.4 Forge"
        )
        assertTrue(
            br.output.contains("modrinth.announcementTitle=Download 3.4.5+neoforge-1.21 from Modrinth"),
            "Modrinth announcement title has not been set correctly for 1.21 Neoforge"
        )

        val versionCount5 = Regex(Regex.escape("curseforge.minecraftVersions=[1.21.4]")).findAll(br.output).count()
        val versionCount6 = Regex(Regex.escape("curseforge.minecraftVersions=[1.21]")).findAll(br.output).count()
        val versionCount7 =
            Regex(Regex.escape("curseforge.accessToken=a-test-curseforge-token-123.123")).findAll(br.output).count()
        val versionCount8 =
            Regex(Regex.escape("curseforge.projectId=an-even-better-test-curseforge-id-456.456")).findAll(br.output)
                .count()

        assertEquals(2, versionCount5, "CurseForge minecraft versions have not been set correctly for 1.21.4")
        assertEquals(1, versionCount6, "CurseForge minecraft versions have not been set correctly for 1.21")
        assertEquals(3, versionCount7, "CurseForge access token has not been set correctly for all versions")
        assertEquals(3, versionCount8, "CurseForge project ID has not been set correctly for all versions")

        assertTrue(
            br.output.contains("curseforge.announcementTitle=Download 3.4.5+fabric-1.21.4 from CurseForge"),
            "CurseForge announcement title has not been set correctly for 1.21.4 Fabric"
        )
        assertTrue(
            br.output.contains("curseforge.announcementTitle=Download 3.4.5+forge-1.21.4 from CurseForge"),
            "CurseForge announcement title has not been set correctly for 1.21.4 Forge"
        )
        assertTrue(
            br.output.contains("curseforge.announcementTitle=Download 3.4.5+neoforge-1.21 from CurseForge"),
            "CurseForge announcement title has not been set correctly for 1.21 Neoforge"
        )
    }
}
