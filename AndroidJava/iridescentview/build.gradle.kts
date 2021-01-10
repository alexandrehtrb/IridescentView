plugins {
    id("com.android.library")
}

android {
    compileSdkVersion(LibConfiguration.compileSdkVersion)
    buildToolsVersion(LibConfiguration.buildToolsVersion)
    defaultConfig {
        minSdkVersion(LibConfiguration.minSdkVersion)
        targetSdkVersion(LibConfiguration.targetSdkVersion)
        versionCode = LibConfiguration.versionCode
        versionName = LibConfiguration.versionName
        testInstrumentationRunner = LibConfiguration.testInstrumentationRunner
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), file("proguard-rules.pro"))
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}