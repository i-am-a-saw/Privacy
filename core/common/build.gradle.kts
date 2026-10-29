plugins {
    alias(libs.plugins.messenger.jvm.library)
    alias(libs.plugins.messenger.hilt)
}

dependencies {
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
}