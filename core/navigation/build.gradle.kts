plugins {
    alias(libs.plugins.messenger.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.iamasaw.privacy.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.core)
}
