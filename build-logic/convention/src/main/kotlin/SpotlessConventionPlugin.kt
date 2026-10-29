import com.diffplug.gradle.spotless.SpotlessExtension
import com.iamasaw.privacy.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.diffplug.spotless")

        val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion
        val ktlintRules = mapOf(
            "ktlint_code_style" to "ktlint_official",
            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
        )

        extensions.configure<SpotlessExtension> {
            kotlin {
                target("src/**/*.kt")
                ktlint(ktlintVersion).editorConfigOverride(ktlintRules)
            }
            kotlinGradle {
                target("*.gradle.kts")
                ktlint(ktlintVersion).editorConfigOverride(ktlintRules)
            }
        }
    }
}