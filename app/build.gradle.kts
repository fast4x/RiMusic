plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    android.buildFeatures.buildConfig=true
    compileSdk = 34

    defaultConfig {
        applicationId = "it.fast4x.rimusic"
        minSdk = 21
        targetSdk = 34
        versionCode = 28
        versionName = "0.6.30"
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

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

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
    implementation(libs.exoplayer)
    implementation(libs.room)
    implementation(project(":lrclib"))
    kapt(libs.room.compiler)
    implementation(projects.innertube)
    implementation(projects.kugou)

    val appcompatVersion = "1.6.1"
    implementation("androidx.appcompat:appcompat:$appcompatVersion")
    implementation("androidx.appcompat:appcompat-resources:$appcompatVersion")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.3.0")
    implementation("androidx.media:media:1.7.0")
    implementation("androidx.compose.material:material:1.6.5")
    implementation("androidx.compose.material3:material3-android:1.2.1")
    implementation("com.github.therealbush:translator:1.0.2")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
    implementation("com.github.lincollincol:compose-audiowaveform:1.1.1")
    //implementation("com.github.lincollincol:amplituda:2.2.2")
    implementation("com.github.KieronQuinn:MonetCompat:0.4.1")
    implementation("androidx.palette:palette:1.0.0")
    implementation("com.google.android.material:material:1.12.0-rc01")
    
    //End
    coreLibraryDesugaring(libs.desugaring)
}
