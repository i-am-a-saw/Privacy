plugins {
    alias(libs.plugins.messenger.android.library)
    alias(libs.plugins.messenger.hilt)
}

android {
    namespace = "com.iamasaw.privacy.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
