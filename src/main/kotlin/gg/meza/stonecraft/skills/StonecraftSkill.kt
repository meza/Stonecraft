package gg.meza.stonecraft.skills

internal const val STONECRAFT_SKILL_DESTINATION = ".agents/skills/stonecraft/SKILL.md"
internal const val STONECRAFT_GUIDANCE_REMINDER_PROPERTY = "stonecraft.showGuidanceReminder"

private const val STONECRAFT_SKILL_RESOURCE = "gg/meza/stonecraft/skills/stonecraft/SKILL.md"

internal enum class StonecraftSkillState {
    MISSING,
    OUTDATED,
    CURRENT,
}

internal data class StonecraftSkillInstallation(
    val state: StonecraftSkillState,
    val version: Int?,
)

internal data class EmbeddedStonecraftSkill(
    val content: String,
    val version: Int,
)

internal object StonecraftSkill {
    val embedded: EmbeddedStonecraftSkill by lazy {
        val content = StonecraftSkill::class.java.classLoader
            .getResourceAsStream(STONECRAFT_SKILL_RESOURCE)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: error("Embedded Stonecraft skill not found at $STONECRAFT_SKILL_RESOURCE")

        val version = extractVersion(content)
            ?: error("Embedded Stonecraft skill must declare a non-negative integer version in its frontmatter")

        EmbeddedStonecraftSkill(content, version)
    }

    fun classify(installedContent: String?, bundledVersion: Int): StonecraftSkillInstallation {
        if (installedContent == null) {
            return StonecraftSkillInstallation(StonecraftSkillState.MISSING, null)
        }

        val installedVersion = extractVersion(installedContent)
        val state = if (installedVersion != null && installedVersion >= bundledVersion) {
            StonecraftSkillState.CURRENT
        } else {
            StonecraftSkillState.OUTDATED
        }

        return StonecraftSkillInstallation(state, installedVersion)
    }

    private fun extractVersion(content: String): Int? {
        val lines = content.lineSequence().iterator()
        if (!lines.hasNext() || lines.next().trimEnd() != "---") {
            return null
        }

        var insideMetadata = false
        var metadataSeen = false
        var metadataChildIndent: Int? = null
        var versionSeen = false
        var version: Int? = null
        while (lines.hasNext()) {
            val line = lines.next()
            if (line.trimEnd() == "---") {
                return version
            }

            if (line.isBlank() || line.trimStart().startsWith('#')) {
                continue
            }

            if (!line.startsWith(' ')) {
                insideMetadata = line.trimEnd() == "metadata:"
                metadataChildIndent = null
                if (insideMetadata) {
                    if (metadataSeen) {
                        return null
                    }
                    metadataSeen = true
                }
                continue
            }

            if (!insideMetadata) {
                continue
            }

            val indentation = line.indexOfFirst { it != ' ' }
            if (indentation < 0) {
                return null
            }

            val directChildIndent = metadataChildIndent ?: indentation.also { metadataChildIndent = it }
            val metadataEntry = line.substring(indentation).trimEnd()
            if (indentation != directChildIndent || !metadataEntry.startsWith("version:")) {
                continue
            }
            if (versionSeen) {
                return null
            }
            versionSeen = true

            val rawValue = metadataEntry.substringAfter(':')
            if (rawValue.isEmpty() || !rawValue.first().isWhitespace()) {
                return null
            }
            val value = rawValue.trim()
            if (value.length < 2 || value.first() != value.last() || value.first() !in setOf('\'', '"')) {
                return null
            }

            version = value.substring(1, value.lastIndex)
                .toIntOrNull()
                ?.takeIf { it >= 0 }
                ?: return null
        }

        return null
    }
}
