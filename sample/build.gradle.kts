plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.zainkhalid91.loaders.sample"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.zainkhalid91.loaders.sample"
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":gallery"))
    implementation(libs.androidx.activity.compose)
}
