plugins {
    alias(libs.plugins.messenger.android.library)
    alias(libs.plugins.messenger.hilt)
}

android {
    namespace = "com.iamasaw.privacy.core.domain"

    flavorDimensions += "environment"
    productFlavors {
        create("local") { dimension = "environment" }
        create("dev") { dimension = "environment" }
        create("demo") { dimension = "environment" }
    }
}

dependencies {
    api(project(":core:data"))
    implementation(libs.javax.inject)
}
