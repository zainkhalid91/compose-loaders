plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}

// On JitPack the group and version must match the coordinates it serves
// (com.github.zainkhalid91.compose-loaders:loaders:<tag>), including inside Gradle module metadata.
val jitpack = System.getenv("JITPACK") == "true"

allprojects {
    group = if (jitpack) "${System.getenv("GROUP")}.${System.getenv("ARTIFACT")}" else "io.github.zainkhalid91"
    version = if (jitpack) System.getenv("VERSION") else "0.2.0"
}
