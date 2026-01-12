import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Load local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    namespace = "com.bethwestsl.devistagram"
    compileSdk {
        version = release(36)
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.bethwestsl.devistagram"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add BuildConfig fields for DeviantArt credentials
        buildConfigField("String", "DEVIANTART_CLIENT_ID", "\"${localProperties.getProperty("DEVIANTART_CLIENT_ID", "")}\"")
        buildConfigField("String", "DEVIANTART_CLIENT_SECRET", "\"${localProperties.getProperty("DEVIANTART_CLIENT_SECRET", "")}\"")
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    
    // Security for encrypted storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Browser for custom tabs
    implementation("androidx.browser:browser:1.7.0")
    
    // Image loading
    implementation("io.coil-kt:coil:2.5.0")
    
    // Image zoom (PhotoView)
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    
    // HTML parsing for web scraping
    implementation("org.jsoup:jsoup:1.17.2")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // ViewPager2 for tabs
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}