package gg.meza.stonecraft

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.intellij.lang.annotations.Language
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption


interface IntegrationTest {
    companion object {
        @Language("gradle")
        val kotlinHeader = """
plugins {
    id("gg.meza.stonecraft")
}
        """.trimIndent()
    }

    class TestBuilder(addHeader: Boolean) {
        private val runner = GradleRunner.create()
            .withPluginClasspath()
            .withArguments("-Dorg.gradle.jvmargs=-Xmx4G")
            .forwardOutput()
            .withDebug(true)

        private val gradleHome: File
        private val projectDir: File
        private val buildScript: File
        private val gradleProperties: File
        private val settingsFile: File
        private val stonecutterGradle: File
        private var arguments = ArrayList<String>()
        private var supportedMinecraftVersions = mutableMapOf<String, List<String>>()

        init {
            val testDir = File("build/e2e_tests")
            val ext = ".kts"
            gradleHome = File(testDir, "home")
            projectDir = File(testDir, "project")
            buildScript = File(projectDir, "build.gradle$ext")
            gradleProperties = File(projectDir, "gradle.properties")
            settingsFile = File(projectDir, "settings.gradle$ext")
            stonecutterGradle = File(projectDir, "stonecutter.gradle$ext")
            val fixturesDir = File("src/test/resources/fixtures")

            projectDir.mkdirs()

            // Clean up
            File(projectDir, "build.gradle").delete()
            File(projectDir, "build.gradle.kts").delete()
            File(projectDir, "settings.gradle").delete()
            File(projectDir, "settings.gradle.kts").delete()
            File(projectDir, "gradle.properties").delete()

            val resources = File(projectDir, "src/main/resources")
            resources.mkdirs()

            if (fixturesDir.exists() && fixturesDir.isDirectory()) {
                Files.walk(fixturesDir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .forEach { source: Path ->
                        try {
                            val destination = projectDir.toPath().resolve(fixturesDir.toPath().relativize(source))
                            destination.toFile().mkdirs()
                            if (!Files.isDirectory(source)) {
                                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
                            }
                        } catch (e: IOException) {
                            throw RuntimeException(e)
                        }
                    }
            }

            if (addHeader) {
                buildScript.appendText(kotlinHeader)
            }


            runner.withProjectDir(projectDir)
            arguments.addAll(
                listOf(
                    "--gradle-user-home", gradleHome.absolutePath,
                    "--stacktrace",
                    "--warning-mode", "all"
                )
            )
        }

        fun setStonecutterVersion(version: String, vararg loaders: String): TestBuilder {
            val versions = loaders.map { it }
            supportedMinecraftVersions[version] = versions
            return this
        }

        fun withProperties(properties: Map<String, String>): TestBuilder {
            properties.forEach { (key, value) ->
                val existingProperties = gradleProperties.readText().lines().filterNot { it.startsWith("$key=") }
                gradleProperties.writeText(existingProperties.joinToString("\n") + "\n$key=$value\n")
            }
            return this
        }

        fun buildScript(@Language("gradle") script: String): TestBuilder {
            buildScript.appendText("\n" + script + "\n")
            return this
        }

        fun project(): Project {
            return ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .build()
        }

        fun build(): BuildResult {

            if (supportedMinecraftVersions.isEmpty()) {
                setStonecutterVersion("1.21.4", "fabric")
            }

            val settings = settingsFile.readText()
            val versions = getSupportedMinecraftVersions()
            val newSettings = settings.replace("STONECUTTER_VERSIONS", versions, true)
            settingsFile.writeText(newSettings)

            val scGradle = stonecutterGradle.readText();
            val newScGradle = scGradle.replace("ACTIVE_VERSION", getFirstVersion(), true)
            stonecutterGradle.writeText(newScGradle)


            runner.withArguments(arguments)
            return runner.run()
        }

        fun run(task: String): BuildResult {
            arguments.add(task)
            return this.build()
        }

        fun setModProperty(key: String, value: String): TestBuilder {
            val properties = gradleProperties.readText().lines().filterNot { it.startsWith(key) }
            gradleProperties.writeText(properties.joinToString("\n") + "\n$key=$value\n")
            return this
        }

        private fun getSupportedMinecraftVersions(): String {
            val versions = supportedMinecraftVersions.map { (version, loaders) ->
                val loadersString = loaders.joinToString(", ") { "\"$it\"" }
                "mc(\"$version\", $loadersString)"
            }

            return versions.joinToString("\n")
        }

        private fun getFirstVersion(): String {
            val key = supportedMinecraftVersions.keys.first()
            val loader = supportedMinecraftVersions[key]?.first()
            return "$key-$loader"
        }
    }

    fun gradleTest(addHeader: Boolean = true): TestBuilder {
        return TestBuilder(addHeader)
    }

    fun Project.setProperties(properties: Map<String, String>) {
        properties.forEach { (key, value) ->
            extensions.extraProperties[key] = value
        }
    }
}
