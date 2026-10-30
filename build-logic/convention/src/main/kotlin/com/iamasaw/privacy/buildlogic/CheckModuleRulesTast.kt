package com.iamasaw.privacy.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault


@DisableCachingByDefault(
    because = "This verification task has no output files."
)
abstract class CheckModuleRulesTask: DefaultTask() {
    @get:Input
    abstract val forbiddenDependencies: ListProperty<String>

    @TaskAction
    fun checkRules() {
        val violations = forbiddenDependencies.get().distinct().sorted()

        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Feature modules must not depend on other feature modules.")
                    violations.forEach { violation ->
                        appendLine(" $violation")
                    }
                    append(
                        "Use shared navigation keys in :core:navigation " +
                         "for navigation between features."
                    )
                }
            )
        }
        logger.lifecycle("Module dependency rules passed.")
    }
}
