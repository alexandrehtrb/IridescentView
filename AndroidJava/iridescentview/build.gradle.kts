import org.jetbrains.kotlin.konan.properties.loadProperties
import org.jetbrains.kotlin.konan.properties.propertyString

plugins {
    id("com.android.library")
    id("com.jfrog.bintray")
    id("maven-publish")
    id("signing")
}

//region Load properties from properties file or environment variables

val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    println("Found secret props file, loading props")
    val properties = loadProperties("${rootDir}\\local.properties")
    ext["signing.keyId"] = properties.propertyString("signing.keyId")
    ext["signing.password"] = properties.propertyString("signing.password")
    ext["signing.secretKeyRingFile"] = properties.propertyString("signing.secretKeyRingFile")
    ext["ossrhUsername"] = properties.propertyString("ossrhUsername")
    ext["ossrhPassword"] = properties.propertyString("ossrhPassword")
} else {
    println("No props file, loading env vars")
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID") ?: ""
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD") ?: ""
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE") ?: ""
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME") ?: ""
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD") ?: ""
}

//endregion

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
                    scm {
                        connection.set(LibConfiguration.scmConnection)
                        developerConnection.set(LibConfiguration.scmDeveloperConnection)
                        url.set(LibConfiguration.scmUrl)
                    }
                }
            }
        }
        repositories {
            maven {
                name = "sonatype" // Sonatype / MavenCentral

                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
                // You only need this if you want to publish snapshots, otherwise just set the URL
                // to the release repo directly

                setUrl(if(version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

                // The username and password we've fetched earlier
                credentials {
                    // Getting OSSRH Maven Central user and key from properties file
                    val properties = loadProperties("${rootDir}\\local.properties")
                    val ossrhUsername = properties.propertyString("ossrhUsername")
                    val ossrhPassword = properties.propertyString("ossrhPassword")

                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
    }
}


signing {
    sign(publishing.publications)
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