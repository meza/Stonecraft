package gg.meza.stonecraft.configurations

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import net.fabricmc.loom.configuration.ide.idea.IdeaSyncTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.named

internal val ACTIVE_CLIENT_INTELLIJ_RUN_CONFIGURATION =
    """
    <component name="ProjectRunConfigurationManager">
      <configuration default="false" name="Run the Active Minecraft Client" type="GradleRunConfiguration" factoryName="Gradle">
        <ExternalSystemSettings>
          <option name="executionName" />
          <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
          <option name="externalSystemIdString" value="GRADLE" />
          <option name="scriptParameters" value="" />
          <option name="taskDescriptions">
            <list />
          </option>
          <option name="taskNames">
            <list>
              <option value=":runActive" />
            </list>
          </option>
          <option name="vmOptions" />
        </ExternalSystemSettings>
        <ExternalSystemDebugServerProcess>false</ExternalSystemDebugServerProcess>
        <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
        <DebugAllEnabled>true</DebugAllEnabled>
        <RunAsTest>false</RunAsTest>
        <method v="2" />
      </configuration>
    </component>
    """.trimIndent() + "\n"

fun configureIntellij(
    project: Project,
    stonecutter: StonecutterBuildExtension,
) {
    if (!stonecutter.current.isActive) {
        return
    }

    val outputFile = project.isolated.rootProject.projectDirectory
        .file(".idea/runConfigurations/Stonecraft_Active_Minecraft_Client.xml")
        .asFile

    project.tasks.named<IdeaSyncTask>("ideaSyncTask") {
        inputs.property("stonecraftActiveMinecraftClient", ACTIVE_CLIENT_INTELLIJ_RUN_CONFIGURATION)
        outputs.file(outputFile)
        doLast {
            outputFile.parentFile.mkdirs()
            outputFile.writeText(ACTIVE_CLIENT_INTELLIJ_RUN_CONFIGURATION, Charsets.UTF_8)
        }
    }
}
