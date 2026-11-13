plugins {
    alias(libs.plugins.messenger.android.library)
    alias(libs.plugins.messenger.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val localKey = providers.gradleProperty("supabase.local.key").orNull.orEmpty()
val devUrl = providers.gradleProperty("supabase.dev.url").orNull.orEmpty()
val devKey = providers.gradleProperty("supabase.dev.key").orNull.orEmpty()
val demoUrl = providers.gradleProperty("supabase.demo.url").orNull.orEmpty()
val demoKey = providers.gradleProperty("supabase.demo.key").orNull.orEmpty()

android {
    namespace = "com.iamasaw.privacy.core.network"

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += "environment"
    productFlavors {
        create("local") {
            dimension = "environment"
            buildConfigField("String", "SUPABASE_URL", "\"http://10.0.2.2:54321\"")
            buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"$localKey\"")
        }
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "SUPABASE_URL", "\"$devUrl\"")
            buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"$devKey\"")
        }
        create("demo") {
            dimension = "environment"
            buildConfigField("String", "SUPABASE_URL", "\"$demoUrl\"")
            buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"$demoKey\"")
        }
    }
}

dependencies {
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.functions)
    implementation(libs.ktor.client.okhttp)
}
