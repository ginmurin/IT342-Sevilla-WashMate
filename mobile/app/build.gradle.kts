plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") // Remove version here
    id("org.jetbrains.kotlin.plugin.serialization") // Remove version here
}

android {
    namespace = "com.example.washmate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.washmate"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SUPABASE_URL", "\"https://hjjcgurxearnkniuxnqd.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"sb_publishable_iuB5PF-p5rfTT40iI45t3g_R_Z6bY7V\"")
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080\"")
        }
        create("staging") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://api-staging.example.com\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://api.example.com\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // Standard AndroidX (Stable for API 34)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Retrofit (Stable 2.x - 3.x is still very new/beta)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Coroutines (Stable for 1.9)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Supabase (Version 2.5.0)
    val supabaseVersion = "2.5.0"
    implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:realtime-kt:$supabaseVersion")
    implementation(platform("io.github.jan-tennert.supabase:bom:$supabaseVersion"))

    // Ktor (2.3.12 for compatibility)
    implementation("io.ktor:ktor-client-android:2.3.12")

    // Secure SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // JSON serialization (Matches Kotlin 1.9)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

