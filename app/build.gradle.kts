plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.wstxda.toolkit"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.wstxda.toolkit"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 211
        versionName = "2.1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.preference)
    implementation(libs.google.material)
    implementation(libs.markdown.core)
    implementation(libs.markdown.linkify)
}