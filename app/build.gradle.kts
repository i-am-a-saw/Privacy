plugins {
    alias(libs.plugins.messenger.android.application)
    alias(libs.plugins.messenger.android.compose)
    alias(libs.plugins.messenger.hilt)
}

android {
    namespace = "com.iamasaw.privacy"

    defaultConfig {
        applicationId = "com.iamasaw.privacy"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
