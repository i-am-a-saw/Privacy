plugins {
    alias(libs.plugins.messenger.android.library)
    alias(libs.plugins.messenger.android.compose)
    alias(libs.plugins.messenger.hilt)
}

android {
    namespace = "com.iamasaw.privacy.feature.onboarding"

    flavorDimensions += "environment"
    productFlavors {
        create("local") { dimension = "environment" }
        create("dev") { dimension = "environment" }
        create("demo") { dimension = "environment" }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.hilt.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(project(":core:model"))
    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
