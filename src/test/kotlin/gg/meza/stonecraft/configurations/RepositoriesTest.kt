package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Test repository setup")
class RepositoriesTest : IntegrationTest {

    @Test
    fun `repositories are correctly added to the project`() {
        val gt = gradleTest().buildScript(
            """
    tasks.register("printRepos") {
        doLast {
            repositories.forEach {
                println(it.name)
            }
        }
    }
            """.trimIndent()
        )

        gt.setStonecutterVersion("1.21.4", "fabric")
        val buildResult = gt.run("printRepos")

        val expectedRepos = listOf(
            "LoomLocalRemappedMods",
            "LoomGlobalMinecraft",
            "LoomLocalMinecraft",
            "LoomTransformedForgeDependencies",
            "Architectury",
            "Fabric",
            "Mojang",
            "Forge"
        )

        expectedRepos.forEach { taskName ->
            assertTrue(buildResult.output.contains(taskName), "Repo $taskName should be present in the config")
        }
    }
}
