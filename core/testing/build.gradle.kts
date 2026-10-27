plugins {
    alias(libs.plugins.messenger.jvm.library)
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}