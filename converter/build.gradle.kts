plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.mavenPublish)
}

group = "net.microink.cl.camerax.converter"
version = "1.0.1"
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = (group.toString())
                artifactId = "CLCameraXConverter"
                version = version
            }
        }
    }
}

android {
    namespace = "net.microink.cl.camerax.converter"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // CameraX系列
    // The following line is optional, as the core library is included indirectly by camera-camera2
    api(libs.androidx.camera.core)
    api(libs.androidx.camera.camera2)
    // If you want to additionally use the CameraX Lifecycle library
    api(libs.androidx.camera.lifecycle)
    // If you want to additionally use the CameraX VideoCapture library
//    implementation(libs.androidx.camera.video)
    // If you want to additionally use the CameraX View class
    api(libs.androidx.camera.view)
    // If you want to additionally add CameraX ML Kit Vision Integration
//    implementation(libs.androidx.camera.mlkit.vision)
    // If you want to additionally use the CameraX Extensions library
    api(libs.androidx.camera.extensions)
}