plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Frame-time comparison of a Compose loader against an equivalent Lottie animation.
// Run tools/benchmark.ps1 with a phone connected. Not published.
android {
    namespace = "io.github.zainkhalid91.loaders.benchmark"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.zainkhalid91.loaders.benchmark"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        // Compose debug builds are several times slower, so only a release build is measured.
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":loaders"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.lottie.compose)
}
