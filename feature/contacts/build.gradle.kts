plugins {
    alias(libs.plugins.messenger.android.library)
    alias(libs.plugins.messenger.android.compose)
    alias(libs.plugins.messenger.hilt)
}

android {
    namespace = "com.iamasaw.privacy.feature.contacts"

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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.hilt.viewmodel.compose)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.zxing.core)

    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
