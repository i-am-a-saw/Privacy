import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ModuleRulesConventionPluginTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `allows feature to core and app to feature dependencies`() {
        val projectDir = createProject()

        writeDependencies(
            projectDir,
            ":feature:auth",
            "implementation project(':core:model')",
        )
        writeDependencies(
            projectDir,
            ":app",
            "implementation project(':feature:auth')",
        )
        val result = runner(projectDir, ":checkModuleRules").build()

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":checkModuleRules")?.outcome,
        )
        assertTrue(result.output.contains("Module dependency rules passed."))
    }

    @Test
    fun `rejects implementation dependency between features`() {
        val projectDir = createProject()

        writeDependencies(
            projectDir,
            ":feature:auth",
            "implementation project(':feature:contacts')",
        )

        val result = runner(projectDir, ":checkModuleRules").buildAndFail()

        assertEquals(
            TaskOutcome.FAILED,
            result.task(":checkModuleRules")?.outcome,
        )
        assertTrue(
            result.output.contains(
                ":feature:auth --implementation--> :feature:contacts",
            ),
        )
    }

    @Test
    fun `rejects api dependency between features`() {
        val projectDir = createProject()
        writeDependencies(
            projectDir,
            ":feature:auth",
            "api project(':feature:contacts')",
        )

        val result = runner(projectDir, ":checkModuleRules").buildAndFail()
        assertEquals(
            TaskOutcome.FAILED,
            result.task(":checkModuleRules")?.outcome,
        )
        assertTrue(
            result.output.contains(
                ":feature:auth --api--> :feature:contacts",
            ),
        )
    }


    @Test
    fun `rejects test dependency between features`() {
        val projectDir = createProject()

        writeDependencies(
            projectDir,
            ":feature:auth",
            "testImplementation project(':feature:contacts')",
        )

        val result = runner(projectDir, ":checkModuleRules").buildAndFail()

        assertEquals(
            TaskOutcome.FAILED,
            result.task(":checkModuleRules")?.outcome,
        )
        assertTrue(
            result.output.contains(
                ":feature:auth --testImplementation--> :feature:contacts",
            ),
        )
    }

    @Test
    fun `root check runs the module rule check`() {
        val projectDir = createProject()

        writeDependencies(
            projectDir,
            ":feature:auth",
            "implementation project(':feature:contacts')",
        )
        val result = runner(projectDir, ":check").buildAndFail()

        assertEquals(
            TaskOutcome.FAILED,
            result.task(":checkModuleRules")?.outcome,
        )
        assertTrue(
            result.output.contains(
                ":feature:auth --implementation--> :feature:contacts",
            ),
        )
    }

    @Test
    fun `reuses configuration cache and detects changed dependencies`() {
        val projectDir = createProject()

        writeDependencies(
            projectDir,
            ":feature:auth",
            "implementation project(':core:model')",
        )

        val firstRun = runner(projectDir, ":checkModuleRules").build()


        assertEquals(
            TaskOutcome.SUCCESS,
            firstRun.task(":checkModuleRules")?.outcome,
        )

        val secondRun = runner(projectDir, ":checkModuleRules").build()

        assertEquals(
            TaskOutcome.SUCCESS,
            secondRun.task(":checkModuleRules")?.outcome,
        )
        assertTrue(secondRun.output.contains("Reusing configuration cache."))

        writeDependencies(
            projectDir,
            ":feature:auth",
            "implementation project(':feature:contacts')",
        )

        val changedRun =
            runner(projectDir, ":checkModuleRules").buildAndFail()

        assertEquals(
            TaskOutcome.FAILED,
            changedRun.task(":checkModuleRules")?.outcome,
        )
        assertTrue(
            changedRun.output.contains(
                ":feature:auth --implementation--> :feature:contacts",
            ),
        )
    }

    private fun createProject(): File {
        val projectDir = temporaryFolder.newFolder()
        val modules =
            listOf(
                ":app",
                ":core:model",
                ":feature:auth",
                ":feature:contacts",
            )

        File(projectDir, "settings.gradle").writeText(
            """ 
              rootProject.name = 'module-rules-test' 
              ${modules.joinToString("\n") { "include '$it'" }} 
              """.trimIndent(),
        )
        File(projectDir, "build.gradle").writeText(
            """ 
              plugins { 
                  id 'messenger.module.rules' 
              } 
              """.trimIndent(),
        )

        modules.forEach { modulePath ->
            val moduleDir =
                File(
                    projectDir,
                    modulePath.removePrefix(":").replace(':', '/'),
                )

            check(moduleDir.mkdirs()) {
                "Could not create module directory: $moduleDir"
            }

            File(moduleDir, "build.gradle").writeText(
                """ 
                  plugins { 
                      id 'java-library' 
                  } 
                  """.trimIndent(),
            )
        }

        return projectDir
    }

    private fun writeDependencies(
        projectDir: File,
        modulePath: String,
        declarations: String,
    ) {
        val moduleDir =
            File(
                projectDir,
                modulePath.removePrefix(":").replace(':', '/'),
            )

        File(moduleDir, "build.gradle").appendText(
            "\n\ndependencies {\n${declarations.trimIndent()}\n}\n",
        )
    }

    private fun runner(
        projectDir: File,
        vararg arguments: String,
    ): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(
                arguments.toList() +
                        listOf(
                            "--configuration-cache",
                            "--configuration-cache-problems=fail",
                            "--stacktrace",
                        ),
            )
}
