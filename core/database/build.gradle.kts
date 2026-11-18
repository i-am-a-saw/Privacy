plugins {
    alias(libs.plugins.messenger.android.library)
    alias(libs.plugins.messenger.hilt)
    alias(libs.plugins.room3)
}

android {
    namespace = "com.iamasaw.privacy.core.database"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core:common"))

    api(libs.room3.runtime)
    implementation(libs.sqlite.bundled)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.room3.compiler)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
}
