package gg.meza.stonecraft.configurations

import gg.meza.stonecraft.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Test Stonecraft guidance tasks")
class AgentSkillConfigurationTest : IntegrationTest {

    private val codingAgentMarkers = mapOf(
        "CLAUDECODE" to "1",
        "CODEX_THREAD_ID" to "test-thread",
        "CURSOR_AGENT" to "1",
    )

    @Test
    fun `task list presents guidance and optional skill installation`() {
        val gradleTest = gradleTest().apply {
            setStonecutterVersion("1.21.4", "fabric")
        }

        val result = gradleTest.run("tasks")

        gradleTest.assertNoGradleFailures(result)
        assertTrue(
            result.output.contains("Help tasks"),
            "Expected the guidance tasks in Gradle's standard help group. Output was:\n${result.output}"
        )
        assertTrue(
            result.output.contains("stonecraftGuidance - Prints Stonecraft and Stonecutter guidance"),
            "Expected the task list to advertise Stonecraft guidance. Output was:\n${result.output}"
        )
        assertTrue(
            result.output.contains("installStonecraftSkill - Installs the Stonecraft skill without replacing"),
            "Expected the task list to advertise skill installation. Output was:\n${result.output}"
        )
        assertTrue(
            result.output.contains("stonecraftGuidanceVersion - Reports the bundled and installed guidance versions"),
            "Expected the task list to advertise the cheap guidance version check. Output was:\n${result.output}"
        )
    }

    @Test
    fun `Stonecraft prints a guidance reminder for recognized coding agents`() {
        codingAgentMarkers.forEach { (marker, value) ->
            val gradleTest = gradleTest().apply {
                setStonecutterVersion("1.21.4", "fabric")
                withoutCodingAgentMarkers()
                withEnvironmentVariables(mapOf(marker to value))
            }

            val result = gradleTest.run("help")

            gradleTest.assertNoGradleFailures(result)
            assertTrue(
                Regex("Using Stonecraft \\S+").containsMatchIn(result.output),
                "Expected Stonecraft to print its version for $marker. Output was:\n${result.output}"
            )
            assertTrue(
                Regex("Using Stonecraft \\S+").findAll(result.output).count() == 1,
                "Expected the Stonecraft banner exactly once for $marker. Output was:\n${result.output}"
            )
            assertTrue(
                result.output.contains("Instructions for using Stonecraft and Stonecutter: run ./gradlew stonecraftGuidance"),
                "Expected the banner to direct $marker users to the guidance task. Output was:\n${result.output}"
            )
        }
    }

    @Test
    fun `Stonecraft suppresses the guidance reminder outside coding agents even when enabled`() {
        val gradleTest = gradleTest().apply {
            setStonecutterVersion("1.21.4", "fabric")
            withoutCodingAgentMarkers()
            withProperties(mapOf("stonecraft.showGuidanceReminder" to "true"))
        }

        val result = gradleTest.run("help")

        gradleTest.assertNoGradleFailures(result)
        assertTrue(
            Regex("Using Stonecraft \\S+").containsMatchIn(result.output),
            "Expected the Stonecraft version banner. Output was:\n${result.output}"
        )
        assertNoGuidanceReminder(result.output, "outside a coding agent")
    }

    @Test
    fun `Stonecraft ignores unsupported and empty coding agent markers`() {
        val unsupportedMarkers = mapOf(
            "CLAUDECODE" to "true",
            "CODEX_THREAD_ID" to " ",
            "CURSOR_AGENT" to " ",
            "STONECRAFT_AGENT" to "1",
        )

        unsupportedMarkers.forEach { (marker, value) ->
            val gradleTest = gradleTest().apply {
                setStonecutterVersion("1.21.4", "fabric")
                withoutCodingAgentMarkers()
                withEnvironmentVariables(
                    mapOf(
                        "STONECRAFT_AGENT" to null,
                        marker to value,
                    )
                )
                withProperties(mapOf("stonecraft.showGuidanceReminder" to "true"))
            }

            val result = gradleTest.run("help")

            gradleTest.assertNoGradleFailures(result)
            assertTrue(
                Regex("Using Stonecraft \\S+").containsMatchIn(result.output),
                "Expected the Stonecraft version banner for $marker. Output was:\n${result.output}"
            )
            assertNoGuidanceReminder(result.output, "$marker=$value")
        }
    }

    @Test
    fun `Stonecraft suppresses the guidance reminder when the installed skill is current`() {
        val gradleTest = gradleTest().apply {
            setStonecutterVersion("1.21.4", "fabric")
            withCodingAgentMarker("CODEX_THREAD_ID", "test-thread")
        }

        val installResult = gradleTest.run("installStonecraftSkill", cacheTask = false)
        gradleTest.assertNoGradleFailures(installResult)

        val result = gradleTest.run("help", cacheTask = false)

        gradleTest.assertNoGradleFailures(result)
        assertTrue(
            Regex("Using Stonecraft \\S+").containsMatchIn(result.output),
            "Expected the Stonecraft version banner. Output was:\n${result.output}"
        )
        assertNoGuidanceReminder(result.output, "for a current installed skill")
    }

    @Test
    fun `consumer can disable the guidance reminder`() {
        val gradleTest = gradleTest().apply {
            setStonecutterVersion("1.21.4", "fabric")
            withProperties(mapOf("stonecraft.showGuidanceReminder" to "false"))
            withCodingAgentMarker("CODEX_THREAD_ID", "test-thread")
        }

        val result = gradleTest.run("help")

        gradleTest.assertNoGradleFailures(result)
        assertTrue(
            Regex("Using Stonecraft \\S+").containsMatchIn(result.output),
            "Expected the Stonecraft version banner. Output was:\n${result.output}"
        )
        assertNoGuidanceReminder(result.output, "when disabled")
    }

    @Test
    fun `Stonecraft banner remains visible at quiet log level`() {
        val gradleTest = gradleTest().apply {
            setStonecutterVersion("1.21.4", "fabric")
            withCodingAgentMarker("CODEX_THREAD_ID", "test-thread")
        }

        val result = gradleTest.run(listOf("--quiet", "help"), cacheTask = false)

        gradleTest.assertNoGradleFailures(result)
        assertTrue(
            Regex("Using Stonecraft \\S+").containsMatchIn(result.output),
            "Expected the Stonecraft banner at quiet log level. Output was:\n${result.output}"
        )
        assertTrue(
            result.output.contains("Instructions for using Stonecraft and Stonecutter: run ./gradlew stonecraftGuidance"),
            "Expected Stonecraft guidance at quiet log level. Output was:\n${result.output}"
        )
    }

    @Test
    fun `guidance version task reports current installation`() {
        val gradleTest = gradleTest().apply {
            setStonecutterVersion("1.21.4", "fabric")
        }
        val installResult = gradleTest.run("installStonecraftSkill", cacheTask = false)
        gradleTest.assertNoGradleFailures(installResult)

        val result = gradleTest.run("stonecraftGuidanceVersion", cacheTask = false)

        gradleTest.assertNoGradleFailures(result)
        assertTrue(result.output.contains("Installed Stonecraft guidance version:") && result.output.contains("(current)"))
    }

    @Test
    fun `guidance version task reports a missing installation`() {
        val gradleTest = gradleTest().apply {
            setStonecutterVersion("1.21.4", "fabric")
        }

        val result = gradleTest.run("stonecraftGuidanceVersion", cacheTask = false)

        gradleTest.assertNoGradleFailures(result)
        assertTrue(result.output.contains("Installed Stonecraft guidance version: not installed"))
    }

    @Test
    fun `install task writes the embedded skill to the repository skill directory`() {
        val gradleTest = gradleTest().apply {
            setStonecutterVersion("1.21.4", "fabric")
        }

        val result = gradleTest.run("installStonecraftSkill")

        gradleTest.assertNoGradleFailures(result)
        val installedSkill = gradleTest.project().file(".agents/skills/stonecraft/SKILL.md")
        assertTrue(installedSkill.isFile, "Expected an installed SKILL.md file.")
        assertTrue(installedSkill.length() > 0, "Expected the installed SKILL.md file not to be empty.")
    }

    @Test
    fun `install task preserves an existing skill by default`() {
        val gradleTest = gradleTest().apply {
            setStonecutterVersion("1.21.4", "fabric")
        }
        val installedSkill = gradleTest.project().file(".agents/skills/stonecraft/SKILL.md")
        val userSkill = "User changes"
        installedSkill.apply {
            parentFile.mkdirs()
            writeText(userSkill)
        }

        val result = gradleTest.run("installStonecraftSkill", cacheTask = false)

        gradleTest.assertNoGradleFailures(result)
        assertEquals(userSkill, installedSkill.readText(), "Expected the existing skill to remain unchanged.")
        assertTrue(
            result.output.contains("left it unchanged"),
            "Expected the install task to explain that it preserved the existing skill. Output was:\n${result.output}"
        )
    }

    @Test
    fun `force overwrite replaces an existing skill`() {
        val gradleTest = gradleTest().apply {
            setStonecutterVersion("1.21.4", "fabric")
        }
        val installedSkill = gradleTest.project().file(".agents/skills/stonecraft/SKILL.md")
        installedSkill.apply {
            parentFile.mkdirs()
            writeText("User changes")
        }

        val result = gradleTest.run(listOf("installStonecraftSkill", "--force-overwrite"), cacheTask = false)

        gradleTest.assertNoGradleFailures(result)
        assertTrue(!installedSkill.readText().contains("User changes"))
    }

    private fun IntegrationTest.TestBuilder.withoutCodingAgentMarkers() {
        withEnvironmentVariables(codingAgentMarkers.keys.associateWith { null })
    }

    private fun IntegrationTest.TestBuilder.withCodingAgentMarker(name: String, value: String) {
        withoutCodingAgentMarkers()
        withEnvironmentVariables(mapOf(name to value))
    }

    private fun assertNoGuidanceReminder(output: String, context: String) {
        val reminders = listOf(
            "Instructions for using Stonecraft and Stonecutter",
            "Updated Stonecraft and Stonecutter guidance is available",
        )
        assertTrue(
            reminders.none(output::contains),
            "Expected no guidance reminder $context. Output was:\n$output"
        )
    }
}
