plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.tt2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tt2"
        minSdk = 34
        targetSdk = 34
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.activity:activity:1.8.2")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")

        testImplementation("junit:junit:4.13.2")

        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

        implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-firestore")
        implementation ("com.google.firebase:firebase-storage:20.3.0")

        implementation("com.github.bumptech.glide:glide:4.16.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

        // CameraX
        val camerax_version = "1.3.1"
        implementation("androidx.camera:camera-core:${camerax_version}")
        implementation("androidx.camera:camera-camera2:${camerax_version}")
        implementation("androidx.camera:camera-lifecycle:${camerax_version}")
        implementation("androidx.camera:camera-video:${camerax_version}")
        implementation("androidx.camera:camera-view:${camerax_version}")
        implementation("androidx.camera:camera-extensions:${camerax_version}")
        
        implementation("com.google.guava:guava:31.1-android")
}