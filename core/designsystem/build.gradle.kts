plugins {
    alias(libs.plugins.messenger.android.library)
    alias(libs.plugins.messenger.android.compose)
}

android {
    namespace = "com.iamasaw.privacy.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
}