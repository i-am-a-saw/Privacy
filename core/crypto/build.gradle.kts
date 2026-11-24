plugins {
    alias(libs.plugins.messenger.android.library)
    alias(libs.plugins.messenger.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.iamasaw.privacy.core.crypto"
}

dependencies {
    api(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
}
