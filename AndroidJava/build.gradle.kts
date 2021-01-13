// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath(Plugins.androidBuildTools)
        classpath(Plugins.kotlinGradlePlugin)
        classpath(Plugins.jcenterBintrayPlugin)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        //mavenLocal()
        //maven("https://dl.bintray.com/alexandrehtrb/Maven")
    }
}

val clean by tasks.creating(Delete::class) {
    delete(rootProject.buildDir)
}