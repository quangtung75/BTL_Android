import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs")
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.qtcoding.btl_android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.qtcoding.btl_android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CLOUDINARY_NAME", "\"${localProperties["cloudinary_name"]}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${localProperties["cloudinary_api_key"]}\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${localProperties["cloudinary_api_secret"]}\"")
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
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    //Cloudinary
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}