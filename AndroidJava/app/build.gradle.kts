plugins {
    id("com.android.application")
}

android {
    compileSdkVersion(AppConfiguration.compileSdkVersion)
    buildToolsVersion(AppConfiguration.buildToolsVersion)
    defaultConfig {
        applicationId = AppConfiguration.applicationId
        minSdkVersion(AppConfiguration.minSdkVersion)
        targetSdkVersion(AppConfiguration.targetSdkVersion)
        versionCode = AppConfiguration.versionCode
        versionName = AppConfiguration.versionName
        testInstrumentationRunner = AppConfiguration.testInstrumentationRunner
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isTestCoverageEnabled = true
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isTestCoverageEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), file("proguard-rules.pro"))
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    //implementation(project(":iridescentview"))

    implementation(Dependencies.androidXAppCompat)
    implementation(Dependencies.iridescentView)
}