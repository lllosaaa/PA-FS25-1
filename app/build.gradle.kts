plugins {
    // These alias references should match Kotlin plugin 1.7.20
    // and Android Gradle plugin versions in your version catalog
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Required for Room annotation processing
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "ch.zhaw.pa_fs25"
    compileSdk = 35

    defaultConfig {
        applicationId = "ch.zhaw.pa_fs25"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Kotlin settings
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    // Align the Compose compiler with your Kotlin version (optional)
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
}

// KAPT configuration for Room
kapt {
    correctErrorTypes = true
    arguments {
        // Enable incremental annotation processing for Room
        arg("room.incremental", "true")
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM + UI libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.core.ktx)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.runner)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Lifecycle ViewModel (Compose integration)
    implementation(libs.androidx.lifecycle.viewmodel.compose.v261)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // -------- ROOM 2.4.3 --------
    // Ensure these point to Room 2.4.3 in your version catalog
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    //navigation
    implementation(libs.androidx.navigation.compose)

    //opneCSV
    implementation(libs.opencsv)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)

    //Color
    implementation(libs.material3)

    //testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlin.test)





}
