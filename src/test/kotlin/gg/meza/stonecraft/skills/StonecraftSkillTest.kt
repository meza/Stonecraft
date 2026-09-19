package gg.meza.stonecraft.skills

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StonecraftSkillTest {

    @Test
    fun `invalid frontmatter versions are outdated`() {
        val invalidSkills = listOf(
            "---\nmetadata:\n  config:\n    version: \"1\"\n---\n",
            "---\nmetadata:\n  version: \"1\"\n",
            "---\nmetadata:\n  version: \"1\"\n  version: \"1\"\n---\n",
            "  ---\nmetadata:\n  version: \"1\"\n---\n",
            "---\nmetadata:\n  version:\"1\"\n---\n",
        )

        invalidSkills.forEach { skill ->
            assertEquals(
                StonecraftSkillState.OUTDATED,
                StonecraftSkill.classify(skill, bundledVersion = 1).state,
            )
        }
    }
}
