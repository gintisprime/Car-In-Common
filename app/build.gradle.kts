plugins {
    alias(libs.plugins.android.application)
    // Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.car_in_common_test2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.car_in_common_test2"
        minSdk = 24
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
    // Material Calendar View
    implementation("com.applandeo:material-calendar-view:1.9.0")

    // AndroidX and Material Design
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.6.1")

    // Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    // Firebase Products
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")

    // Google Play Services
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.0.0") // Remove if not using maps

    // Lottie Animations
    implementation("com.airbnb.android:lottie:6.1.0")

    // OBD Java API (If Used)
    implementation(files("src/main/libs/obd-java-api-1.1-SNAPSHOT.jar"))
    implementation(libs.swiperefreshlayout) // Remove if unused

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
