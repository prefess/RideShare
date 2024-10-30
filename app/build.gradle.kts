plugins {
    alias(libs.plugins.android.application)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.rideshare"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rideshare"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    implementation ("com.google.android.material:material:1.4.0")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("androidx.fragment:fragment:1.3.6")
    implementation("com.google.firebase:firebase-core:11.0.2")
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-database:21.0.0")
    implementation ("com.google.android.gms:play-services-auth:21.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.firebase:firebase-storage:20.1.0")
    implementation ("com.google.firebase:firebase-messaging:23.0.0")
    implementation ("com.google.gms:google-services:4.4.2")
    implementation ("androidx.core:core:1.8.0")

    implementation ("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")

    implementation ("androidx.appcompat:appcompat:1.3.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.2")


}