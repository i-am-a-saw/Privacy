import com.iamasaw.privacy.buildlogic.CheckModuleRulesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.register

class ModuleRulesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        require(this == this.rootProject) {
            "Apply messenger.module.rules only to the root project."
        }

        pluginManager.apply("base")

        val moduleCheck = tasks.register<CheckModuleRulesTask>("checkModuleRules") {
            group = "verification"
            description = "Check that feature modules do not depend on each other"
            forbiddenDependencies.convention(emptyList<String>())
        }

        subprojects.forEach { sourceProject ->
            val sourcePath = sourceProject.path

            if (sourcePath.startsWith(":feature:")) {
                sourceProject.configurations.all {
                    val configurationName = name

                    dependencies.withType(ProjectDependency::class.java).all {
                        val destinationPath = path

                        if (destinationPath.startsWith(":feature:") && destinationPath != sourcePath) {
                            val violation = "$sourcePath --$configurationName--> $destinationPath"

                            moduleCheck.configure {
                                forbiddenDependencies.add(violation)
                            }
                        }
                    }
                }
            }
        }

        tasks.named("check") {
            dependsOn(moduleCheck)
        }
    }
}
