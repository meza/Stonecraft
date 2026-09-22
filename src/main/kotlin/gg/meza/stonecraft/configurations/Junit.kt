package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.data.ParsedVersion
import gg.meza.stonecraft.extension.ModSettingsExtension
import gg.meza.stonecraft.mod
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import java.io.File

private const val JUNIT_JUPITER_VERSION = "5.10.2"
private const val JUNIT_PLATFORM_VERSION = "1.10.2"

internal enum class JunitSupportMode {
    PLATFORM_ONLY,
    FABRIC_LOADER,
    NEOFORGE_FML_FIXTURES,
}

internal object JunitCompatibility {
    private val fabricLoaderJunitBoundary = ParsedVersion("0.14.15")
    private val neoForgeTestFixturesBoundary = ParsedVersion("20.6.122")

    fun resolve(
        loader: String,
        loaderVersion: String?,
    ): JunitSupportMode = when (loader) {
        "fabric" -> resolveVersionedMode(
            version = loaderVersion,
            boundary = fabricLoaderJunitBoundary,
            supportedMode = JunitSupportMode.FABRIC_LOADER,
        )

        "neoforge" -> resolveVersionedMode(
            version = loaderVersion,
            boundary = neoForgeTestFixturesBoundary,
            supportedMode = JunitSupportMode.NEOFORGE_FML_FIXTURES,
        )

        else -> JunitSupportMode.PLATFORM_ONLY
    }

    private fun resolveVersionedMode(
        version: String?,
        boundary: ParsedVersion,
        supportedMode: JunitSupportMode,
    ): JunitSupportMode {
        val supported = version
            ?.let { runCatching { ParsedVersion(it) >= boundary }.getOrDefault(false) }
            ?: false
        return if (supported) supportedMode else JunitSupportMode.PLATFORM_ONLY
    }
}

fun configureJunit(project: Project, modSettings: ModSettingsExtension) {
    project.afterEvaluate {
        if (!modSettings.enableJunitProp.get()) return@afterEvaluate

        project.dependencies.add(
            "testImplementation",
            "org.junit.jupiter:junit-jupiter:$JUNIT_JUPITER_VERSION"
        )
        project.dependencies.add(
            "testRuntimeOnly",
            "org.junit.platform:junit-platform-launcher:$JUNIT_PLATFORM_VERSION"
        )
        project.tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }

        val loaderVersion = when {
            project.mod.isFabric -> project.mod.prop("loader_version", "")
            project.mod.isNeoforge -> project.mod.prop("neoforge_version", "")
            else -> null
        }
        when (
            JunitCompatibility.resolve(
                loader = project.mod.loader,
                loaderVersion = loaderVersion,
            )
        ) {
            JunitSupportMode.FABRIC_LOADER -> configureFabricLoaderJunit(project, requireNotNull(loaderVersion))
            JunitSupportMode.NEOFORGE_FML_FIXTURES -> configureNeoForgeJunit(project, requireNotNull(loaderVersion))
            JunitSupportMode.PLATFORM_ONLY -> Unit
        }
    }
}

private fun configureFabricLoaderJunit(project: Project, loaderVersion: String) {
    project.dependencies.add(
        "testImplementation",
        "net.fabricmc:fabric-loader-junit:$loaderVersion"
    )
}

private fun configureNeoForgeJunit(project: Project, neoForgeVersion: String) {
    val neoForgeTestFixtures = project.dependencies.create(
        "net.neoforged:neoforge:$neoForgeVersion"
    ) as ExternalModuleDependency
    neoForgeTestFixtures.capabilities {
        requireCapability("net.neoforged:neoforge-moddev-test-fixtures")
    }
    project.dependencies.add("testRuntimeOnly", neoForgeTestFixtures)

    val sourceSets = project.extensions.getByType<SourceSetContainer>()
    val modFolders = listOf("main", "test")
        .flatMap { sourceSetName -> sourceSets.getByName(sourceSetName).output.files }
        .distinct()
        .joinToString(File.pathSeparator) { output -> "main%%${output.absolutePath}" }

    project.tasks.withType<Test>().configureEach {
        jvmArgs("--add-opens=java.base/java.lang.invoke=ALL-UNNAMED")
        systemProperty("fml.modFolders", modFolders)
    }
}
