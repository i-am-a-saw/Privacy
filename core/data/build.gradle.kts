plugins {
    alias(libs.plugins.messenger.android.library)
    alias(libs.plugins.messenger.hilt)
}

android {
    namespace = "com.iamasaw.privacy.core.data"

    flavorDimensions += "environment"
    productFlavors {
        create("local") { dimension = "environment" }
        create("demo") { dimension = "environment" }
        create("dev") { dimension = "environment" }
    }
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    implementation(project(":core:network"))

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)

    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
