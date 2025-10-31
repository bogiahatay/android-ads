plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "net.iblankdigital.ads"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")

    implementation("com.google.android.gms:play-services-ads:24.7.0")
    implementation("com.google.ads.mediation:applovin:13.5.0.0")
    implementation("com.google.ads.mediation:unity:4.16.3.0")

    implementation("com.applovin:applovin-sdk:13.5.0")
    implementation("com.applovin.mediation:google-adapter:24.7.0.0")
    implementation("com.applovin.mediation:unityads-adapter:4.16.3.0")

    implementation("com.unity3d.ads:unity-ads:4.16.3")
}