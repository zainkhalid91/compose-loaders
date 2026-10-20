import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    android {
        namespace = "io.github.zainkhalid91.loaders.gallery"
        compileSdk = 37
        minSdk = 23
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":loaders"))
            implementation(libs.compose.ui.tooling.preview)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

// ./gradlew :gallery:recordFrames renders every loader to PNG frames for docs/ and visual checks.
val desktopMain = kotlin.jvm("desktop").compilations["main"]
tasks.register<JavaExec>("recordFrames") {
    description = "Renders each loader frame by frame to build/frames"
    classpath(desktopMain.output.allOutputs, desktopMain.runtimeDependencyFiles)
    mainClass.set("io.github.zainkhalid91.loaders.gallery.RecordKt")
    args(layout.buildDirectory.dir("frames").get().asFile.absolutePath)
}

compose.desktop {
    application {
        mainClass = "io.github.zainkhalid91.loaders.gallery.MainKt"
    }
}
