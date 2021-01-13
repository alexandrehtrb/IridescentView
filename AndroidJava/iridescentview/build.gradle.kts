import org.jetbrains.kotlin.konan.properties.loadProperties
import org.jetbrains.kotlin.konan.properties.propertyString

plugins {
    id("com.android.library")
    id("com.jfrog.bintray")
    id("maven-publish")
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
            isMinifyEnabled = false
            //proguardFiles(getDefaultProguardFile("proguard-android.txt"), file("proguard-rules.pro"))
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = LibConfiguration.groupId
                artifactId = LibConfiguration.artifactId
                version = LibConfiguration.versionName
                artifact(sourcesJar)

                pom {
                    name.set(LibConfiguration.libraryName)
                    description.set(LibConfiguration.libraryDescription)
                    url.set(LibConfiguration.projectUrl)
                    licenses {
                        license {
                            name.set(LibConfiguration.licenseName)
                            url.set(LibConfiguration.licenseUrl)
                        }
                    }
                    developers {
                        developer {
                            id.set(LibConfiguration.developerId)
                            name.set(LibConfiguration.developerName)
                        }
                    }
                }
            }
        }
    }
}

bintray {
    // Getting bintray user and key from properties file or command line
    val properties = loadProperties("${rootDir}\\local.properties")

    user = properties.propertyString("bintray_user")
    key = properties.propertyString("bintray_apikey")

    // Automatic publication enabled
    publish = true

    override = true

    // Set maven publication onto bintray plugin
    setPublications("release")

    // Configure package
    pkg.apply {
        repo = LibConfiguration.bintrayRepoName
        name = LibConfiguration.libraryName
        setLicenses(LibConfiguration.licenseName)
        setLabels(LibConfiguration.libraryLabel1,
                  LibConfiguration.libraryLabel2,
                  LibConfiguration.libraryLabel3,
                  LibConfiguration.libraryLabel4,
                  LibConfiguration.libraryLabel5,
                  LibConfiguration.libraryLabel6,
                  LibConfiguration.libraryLabel7)
        websiteUrl = LibConfiguration.projectUrl
        vcsUrl = LibConfiguration.vcsUrl
        publicDownloadNumbers = true
        // Configure version
        version.apply {
            name = LibConfiguration.versionName
            released = LibConfiguration.releasedDate
        }
    }
}