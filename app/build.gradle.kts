plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.gms.gradle)
}

fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}

val isAppDebuggable = System.getenv("CI") != "true"

android {
    namespace = "nl.govroam.govsecureid"
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    val gitTagCount = "git tag --list".runCommand().split('\n').size
    val gitTag = "git describe --tags --dirty".runCommand()
    val gitCoreSha = "git submodule status".runCommand().substring(0, 8)
    val ciRunCount = System.getenv("GITHUB_RUN_NUMBER")?.toInt() ?: 0

    defaultConfig {
        applicationId = "nl.govroam.govconext.govsecureid"
        minSdk = libs.versions.android.sdk.min.get().toInt()
        targetSdk = libs.versions.android.sdk.target.get().toInt()
        versionCode = gitTagCount + ciRunCount
        versionName = gitTag.trim().drop(1) + " core($gitCoreSha)"

        testInstrumentationRunner = "org.tiqr.authenticator.runner.HiltAndroidTestRunner"

        manifestPlaceholders["tiqr_config_base_url"] = "https://demo.tiqr.org"
        manifestPlaceholders["tiqr_config_token_exchange_base_url"] = "https://tx.tiqr.org/"
        manifestPlaceholders["tiqr_config_protocol_version"] = "2"
        manifestPlaceholders["tiqr_config_protocol_compatibility_mode"] = "true"
        manifestPlaceholders["tiqr_config_enroll_path_param"] = "tiqrenroll"
        manifestPlaceholders["tiqr_config_auth_path_param"] = "tiqrauth"
        manifestPlaceholders["tiqr_config_enroll_scheme"] = "tiqrenroll"
        manifestPlaceholders["tiqr_config_auth_scheme"] = "tiqrauth"
        manifestPlaceholders["tiqr_config_token_exchange_enabled"] = "false"
        manifestPlaceholders["tiqr_config_in_app_update_check_enabled"] = "true"

        // only package supported languages
        resourceConfigurations += listOf(
            "en",
            "nl",
        )
    }

    buildTypes {
        release {
            isDebuggable = isAppDebuggable
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (isAppDebuggable) {
                signingConfigs.getByName("debug")
            } else {
                null
            }
            manifestPlaceholders["tiqr_config_enforce_challenge_hosts"] = "tiqr.acc.govconext.nl"
        }
        debug {
            isDebuggable = isAppDebuggable
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".staging"
            signingConfig = if (isAppDebuggable) {
                signingConfigs.getByName("debug")
            } else {
                null
            }
            manifestPlaceholders["tiqr_config_enforce_challenge_hosts"] = "govsecureid.govconext.nl"
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
        dataBinding = true
        buildConfig = true
    }
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":core"))

    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.coil)
}

// Disable analytics
configurations {
    all {
        exclude(group = "com.google.firebase", module = "firebase-core")
        exclude(group = "com.google.firebase", module = "firebase-analytics")
        exclude(group = "com.google.firebase", module = "firebase-measurement-connector")
    }
}