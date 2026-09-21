package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("Test repository setup")
class RepositoriesTest : IntegrationTest {

    @TempDir
    lateinit var repositoryDirectory: Path

    @Test
    fun `loom owns shared repositories and stonecraft adds only neoforge`() {
        val gt = gradleTest().buildScript(
            """
    import org.gradle.api.artifacts.repositories.MavenArtifactRepository

    val repositoryDescriptions = repositories.filterIsInstance<MavenArtifactRepository>().map {
        "STONECRAFT_REPOSITORY|${'$'}{project.name}|${'$'}{it.name}|${'$'}{it.url}"
    }

    tasks.register("printRepositories") {
        doLast {
            repositoryDescriptions.forEach(::println)
        }
    }
            """.trimIndent()
        )

        gt.setStonecutterVersion("1.21", "neoforge")
        gt.setStonecutterVersion("1.21.4", "fabric", "forge", "neoforge")
        val buildResult = gt.run("printRepositories")
        gt.assertNoGradleFailures(buildResult)

        val repositoriesByProject = buildResult.output.lineSequence()
            .filter { it.startsWith(REPOSITORY_MARKER) }
            .map { line ->
                val (projectName, repositoryName, repositoryUrl) = line
                    .removePrefix(REPOSITORY_MARKER)
                    .split("|", limit = 3)
                Repository(projectName, repositoryName, repositoryUrl)
            }
            .groupBy(Repository::projectName)

        val expectedProjects = mapOf(
            "1.21-neoforge" to true,
            "1.21.4-fabric" to false,
            "1.21.4-forge" to false,
            "1.21.4-neoforge" to true,
        )

        expectedProjects.forEach { (projectName, expectsNeoForgeRepository) ->
            val repositories = repositoriesByProject.getValue(projectName)

            assertLoomRepositoriesPresent(repositories)
            LOOM_REMOTE_REPOSITORY_URLS.forEach { url ->
                assertEquals(
                    1,
                    repositories.count { it.url == url },
                    "$projectName should contain $url exactly once"
                )
            }

            if (expectsNeoForgeRepository) {
                assertEquals(
                    1,
                    repositories.count { it.name == "NeoForge" && it.url == NEOFORGE_REPOSITORY_URL },
                    "$projectName should contain the filtered NeoForge repository exactly once"
                )
            } else {
                assertFalse(
                    repositories.any { it.url == NEOFORGE_REPOSITORY_URL },
                    "$projectName should not contain the NeoForge repository"
                )
            }
        }
    }

    @Test
    fun `neoforge repository serves only its required groups`() {
        publishArtifact("net.neoforged", "neoforge-test")
        publishArtifact("cpw.mods", "launcher-test")
        publishArtifact("com.example", "unrelated-test")

        val project = ProjectBuilder.builder()
            .withName("1.21.4-neoforge")
            .build()
        configureDependencyRepositories(project)
        val neoForgeRepository = project.repositories.getByName("NeoForge") as MavenArtifactRepository
        neoForgeRepository.url = repositoryDirectory.toUri()

        listOf(
            "net.neoforged:neoforge-test:1.0",
            "cpw.mods:launcher-test:1.0",
        ).forEach { notation ->
            val files = project.configurations.detachedConfiguration(project.dependencies.create(notation)).resolve()
            assertEquals(1, files.size, "$notation should resolve from NeoForge Maven")
        }

        assertThrows(Exception::class.java) {
            project.configurations
                .detachedConfiguration(project.dependencies.create("com.example:unrelated-test:1.0"))
                .resolve()
        }
    }

    private fun assertLoomRepositoriesPresent(repositories: List<Repository>) {
        val expectedNames = listOf(
            "LoomLocalRemappedMods",
            "LoomGlobalMinecraft",
            "LoomLocalMinecraft",
            "LoomTransformedForgeDependencies",
            "Architectury",
            "Fabric",
            "Mojang",
            "Forge"
        )

        expectedNames.forEach { repositoryName ->
            assertTrue(
                repositories.any { it.name == repositoryName },
                "Loom repository $repositoryName should be present"
            )
        }
    }

    private fun publishArtifact(group: String, artifact: String) {
        val artifactDirectory = repositoryDirectory
            .resolve(group.replace('.', '/'))
            .resolve(artifact)
            .resolve("1.0")
        Files.createDirectories(artifactDirectory)
        Files.writeString(
            artifactDirectory.resolve("$artifact-1.0.pom"),
            """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>$group</groupId>
                    <artifactId>$artifact</artifactId>
                    <version>1.0</version>
                </project>
            """.trimIndent()
        )
        Files.write(artifactDirectory.resolve("$artifact-1.0.jar"), byteArrayOf())
    }

    private data class Repository(
        val projectName: String,
        val name: String,
        val url: String,
    )

    private companion object {
        const val REPOSITORY_MARKER = "STONECRAFT_REPOSITORY|"
        const val NEOFORGE_REPOSITORY_URL = "https://maven.neoforged.net/releases/"

        val LOOM_REMOTE_REPOSITORY_URLS = listOf(
            "https://repo.maven.apache.org/maven2/",
            "https://maven.fabricmc.net/",
            "https://maven.architectury.dev/",
            "https://maven.minecraftforge.net/",
        )
    }
}
