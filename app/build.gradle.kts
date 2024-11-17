plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "net.microink.cl.camerax.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.microink.cl.camerax.app"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        // noinspection DataBindingWithoutKapt
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.activity.ktx)

    // lifecycle系列
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // Saved state module for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    // optional - helpers for implementing LifecycleOwner in a Service
    implementation(libs.androidx.lifecycle.service)
    // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
    implementation(libs.androidx.lifecycle.process)


    // CameraX
    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    // If you want to additionally use the CameraX Lifecycle library
    implementation(libs.androidx.camera.lifecycle)
    // If you want to additionally use the CameraX VideoCapture library
//    implementation(libs.androidx.camera.video)
    // If you want to additionally use the CameraX View class
    implementation(libs.androidx.camera.view)
    // If you want to additionally add CameraX ML Kit Vision Integration
//    implementation(libs.androidx.camera.mlkit.vision)
    // If you want to additionally use the CameraX Extensions library
    implementation(libs.androidx.camera.extensions)

    implementation(project(":converter"))
}