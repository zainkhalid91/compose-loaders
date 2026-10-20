import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    `maven-publish`
}

kotlin {
    explicitApi()
    // Kotlin 2.1 metadata and stdlib keep the library usable from apps on Kotlin 2.0 and newer.
    coreLibrariesVersion = "2.1.20"
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
    }

    android {
        namespace = "io.github.zainkhalid91.loaders"
        compileSdk = 36
        minSdk = 23
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.lib.compose.runtime)
            api(libs.lib.compose.foundation)
            api(libs.lib.compose.ui)
            implementation(libs.lib.compose.animation)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("Compose Loaders")
            description.set("A growing collection of loading indicators written in pure Compose.")
            url.set("https://github.com/zainkhalid91/compose-loaders")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
        }
    }
}
