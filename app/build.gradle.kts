/*
plugins {
    id("com.android.application")
    kotlin("android")
    //kotlin("kapt")
    kotlin("ksp")
}

 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.ksp)
    //alias(libs.plugins.kotlin.compose)
}

android {
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    android.buildFeatures.buildConfig=true
    compileSdk = 34

    defaultConfig {
        applicationId = "it.fast4x.rimusic"
        minSdk = 21
        targetSdk = 34
        versionCode = 39
        versionName = "0.6.40"
        //buildConfigField("String", "VERSION_NAME", "\"$versionName\"" )
    }

    splits {
        abi {
            reset()
            isUniversalApk = true
        }
    }

    namespace = "it.fast4x.rimusic"

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appName"] = "RiMusic-Debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["appName"] = "RiMusic"
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
        jvmTarget = "17"
    }

    androidResources {
        generateLocaleConfig = true
    }



}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

/*
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}
 */

/*
android {
    lint {
        baseline = file("lint-baseline.xml")
        //checkReleaseBuilds = false
    }
}
*/

dependencies {
    implementation(projects.composePersist)
    implementation(projects.composeRouting)
    implementation(projects.composeReordering)
    implementation(libs.compose.activity)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ripple)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.coil)
    implementation(libs.palette)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.appcompat)
    implementation(libs.appcompat.resources)
    implementation(libs.core.splashscreen)
    implementation(libs.media)
    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.compose.ui.graphics.android)
    implementation(libs.constraintlayout)
    implementation(libs.runtime.livedata)
    implementation(libs.core.ktx)
    implementation(libs.compose.animation)
    implementation(libs.translator)
    implementation(libs.kotlin.csv)
    implementation(libs.monetcompat)
    implementation(libs.androidmaterial)
    implementation(libs.navigation)
    implementation(libs.smarttoast)
    implementation(libs.timber)
    implementation(libs.crypto)
    implementation(libs.logging.interceptor)

    implementation(libs.room)
    ksp(libs.room.compiler)
    //kapt(libs.room.compiler)

    implementation(projects.innertube)
    implementation(projects.innertubes)
    implementation(projects.kugou)
    implementation(projects.lrclib)
    implementation(projects.piped)

    //val appcompatVersion = "1.7.0"
    //implementation("androidx.appcompat:appcompat:$appcompatVersion")
    //implementation("androidx.appcompat:appcompat-resources:$appcompatVersion")
    //implementation("androidx.core:core-splashscreen:1.0.1")
    //implementation("androidx.media3:media3-datasource-okhttp:1.3.1")
    //implementation("androidx.media:media:1.7.0")
    //implementation("androidx.compose.material:material:1.6.7")
    //implementation("androidx.compose.material3:material3-android:1.3.0-beta02")
    //implementation("androidx.compose.ui:ui-graphics-android:1.6.7")
    //implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    //implementation("androidx.compose.runtime:runtime-livedata:1.6.7")
    //implementation("androidx.core:core-ktx:1.13.1")
    //implementation("androidx.compose.animation:animation:1.6.7")
    //implementation("com.github.therealbush:translator:1.0.2")
    //implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
    //implementation("com.github.KieronQuinn:MonetCompat:0.4.1")
    //implementation("androidx.palette:palette:1.0.0")
    //implementation("com.google.android.material:material:1.12.0")
    //implementation("androidx.navigation:navigation-compose:2.7.7")
    //implementation("io.github.vincent-series:smart-toast:4.1.6")
    //implementation("com.jakewharton.timber:timber:5.0.1")
    //implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    //End
    coreLibraryDesugaring(libs.desugaring)
}
