plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

fun isNewArchitectureEnabled(): Boolean {
    return rootProject.hasProperty("newArchEnabled") && rootProject.property("newArchEnabled") == "true"
}

fun getExtOrDefault(name: String): String {
    return if (rootProject.extra.has(name)) {
        rootProject.extra.get(name) as String
    } else {
        project.properties["MonterosaSdk_$name"] as String
    }
}

fun getExtOrIntegerDefault(name: String): Int {
    return if (rootProject.extra.has(name)) {
        (rootProject.extra.get(name) as? Int) ?: (rootProject.extra.get(name) as String).toInt()
    } else {
        (project.properties["MonterosaSdk_$name"] as String).toInt()
    }
}

android {
    namespace = "co.monterosa.sdk"

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifestNew.xml")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileSdk = getExtOrIntegerDefault("compileSdkVersion")

    defaultConfig {
        minSdk = getExtOrIntegerDefault("minSdkVersion")
        buildConfigField("boolean", "IS_NEW_ARCHITECTURE_ENABLED", isNewArchitectureEnabled().toString())
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    lint {
        targetSdk = getExtOrIntegerDefault("targetSdkVersion")
        disable += "GradleCompatible"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

val kotlinVersion = getExtOrDefault("kotlinVersion")

dependencies {
    implementation("com.facebook.react:react-android")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    // Android SDK
    implementation("co.monterosa.sdk:core:0.17.0")
    implementation("co.monterosa.sdk:launcherkit:0.17.0")
    implementation("co.monterosa.sdk:identifykit:0.17.0")
}