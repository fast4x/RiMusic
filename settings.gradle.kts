/*
 * RiMusic Gradle Settings
 * Created by sarathiq
 * Last modified: 2025-05-14
 */

// 🔧 Core Configuration
rootProject.name = "RiMusic"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// 📊 Build Scan Configuration
plugins {
    id("com.gradle.enterprise") version "3.16.1"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

// 🔌 Repository Management
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        }
        maven {
            url = uri("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        }
        maven {
            url = uri("https://androidx.dev/storage/compose-compiler/repository")
        }
        maven {
            url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { 
            url = uri("https://jitpack.io") 
        }
        maven { 
            url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") 
        }
        maven { 
            url = uri("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental") 
        }
        maven { 
            url = uri("https://androidx.dev/storage/compose-compiler/repository") 
        }
        maven { 
            url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev") 
        }
    }

    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

// 🧩 Module Management
object Modules {
    object Core {
        const val APP = ":composeApp"
        const val PERSIST = ":compose-persist"
    }
    
    object Extensions {
        const val BROTLI = ":ktor-client-brotli"
        const val KUGOU = ":kugou"
        const val LRCLIB = ":lrclib"
        const val PIPED = ":piped"
        const val INVIDIOUS = ":invidious"
    }
}

// Include core modules
include(Modules.Core.APP)
include(Modules.Core.PERSIST)

// Include and configure extension modules
include(Modules.Extensions.BROTLI)
project(Modules.Extensions.BROTLI).projectDir = file("extensions/ktor-client-brotli")

include(Modules.Extensions.KUGOU)
project(Modules.Extensions.KUGOU).projectDir = file("extensions/kugou")

include(Modules.Extensions.LRCLIB)
project(Modules.Extensions.LRCLIB).projectDir = file("extensions/lrclib")

include(Modules.Extensions.PIPED)
project(Modules.Extensions.PIPED).projectDir = file("extensions/piped")

include(Modules.Extensions.INVIDIOUS)
project(Modules.Extensions.INVIDIOUS).projectDir = file("extensions/invidious")
