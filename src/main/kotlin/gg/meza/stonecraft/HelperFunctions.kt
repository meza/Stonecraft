package gg.meza.stonecraft

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.gradle.api.Project
import java.io.InputStreamReader
import java.math.BigDecimal
import java.math.BigInteger

@JvmInline
value class PackFormatVersion private constructor(private val value: BigDecimal) : Comparable<PackFormatVersion> {
    companion object {
        fun of(value: BigDecimal): PackFormatVersion = PackFormatVersion(value.stripTrailingZeros())
        fun of(number: Number): PackFormatVersion = when (number) {
            is BigDecimal -> of(number)
            is BigInteger -> of(number.toBigDecimal())
            is Long -> of(BigDecimal.valueOf(number))
            is Int -> of(BigDecimal.valueOf(number.toLong()))
            is Short -> of(BigDecimal.valueOf(number.toLong()))
            is Byte -> of(BigDecimal.valueOf(number.toLong()))
            is Double -> of(BigDecimal.valueOf(number))
            is Float -> of(BigDecimal.valueOf(number.toDouble()))
            else -> of(BigDecimal(number.toString()))
        }
    }

    fun toIntExact(): Int = try {
        value.intValueExact()
    } catch (ex: ArithmeticException) {
        throw IllegalStateException("Pack format $value cannot be represented as an Int", ex)
    }

    fun toBigDecimal(): BigDecimal = value

    fun toDouble(): Double = value.toDouble()

    fun isWholeNumber(): Boolean = runCatching { value.intValueExact() }.isSuccess

    override fun compareTo(other: PackFormatVersion): Int = value.compareTo(other.value)

    operator fun compareTo(other: Int): Int = value.compareTo(BigDecimal.valueOf(other.toLong()))

    override fun toString(): String = value.stripTrailingZeros().toPlainString()
}

fun String.upperCaseFirst() = replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }
private data class PackFormats(val datapack: PackFormatVersion, val resourcepack: PackFormatVersion)

private object ResourceHolder

private val packFormatMap: Map<String, PackFormats> by lazy {
    val stream = requireNotNull(
        ResourceHolder::class.java.classLoader.getResourceAsStream("pack_versions.json")
    ) { "pack_versions.json not found" }
    val json = JsonParser.parseReader(InputStreamReader(stream)).asJsonObject
    json.entrySet().associate { (version, data) ->
        version to data.asJsonObject.toPackFormats()
    }
}

private fun JsonObject.toPackFormats(): PackFormats {
    return PackFormats(
        PackFormatVersion.of(get("datapack").asBigDecimal),
        PackFormatVersion.of(get("resourcepack").asBigDecimal)
    )
}

private fun getPackFormats(version: String): PackFormats {
    return packFormatMap[version]
        ?: throw IllegalArgumentException("Unknown Minecraft version: $version")
}

fun getResourcePackFormat(version: String): PackFormatVersion = getPackFormats(version).resourcepack

fun getDatapackFormat(version: String): PackFormatVersion = getPackFormats(version).datapack

fun getResourceVersionFor(version: String): Int = getResourcePackFormat(version).toIntExact()

fun getProgramArgs(vararg lists: List<String>): List<String> = lists.flatMap { it }

fun applyIfAbsent(pluginId: String, project: Project) {
    if (!project.pluginManager.hasPlugin(pluginId)) {
        project.pluginManager.apply(pluginId)
    }
}

enum class Side {
    CLIENT,
    SERVER
}
