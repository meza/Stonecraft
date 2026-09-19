package gg.meza.stonecraft.tasks

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class InstallStonecraftSkillTest {

    @TempDir
    lateinit var projectDirectory: Path

    @Test
    fun `only one concurrent non-force installation creates the skill`() {
        val destination = projectDirectory.resolve(".agents/skills/stonecraft/SKILL.md")
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)

        try {
            val attempts = listOf("first skill", "second skill").map { content ->
                executor.submit<StonecraftSkillWriteResult> {
                    start.await()
                    writeStonecraftSkill(destination, content, forceOverwrite = false)
                }
            }

            start.countDown()
            val results = attempts.map { it.get() }

            assertEquals(1, results.count { it == StonecraftSkillWriteResult.CREATED })
            assertEquals(1, results.count { it == StonecraftSkillWriteResult.PRESERVED_EXISTING })
            assertTrue(destination.toFile().readText() in listOf("first skill", "second skill"))
        } finally {
            executor.shutdownNow()
        }
    }
}
