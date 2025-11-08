
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlinSerialization)
    kotlin("kapt")
    alias(libs.plugins.ksp.devtool)

}

val major = 1
val minor = 0
val patch = 0
val suffix = "beta"

val appVersionCode = (major * 1000000) + (minor * 10000) + (patch * 100)
var appVersionName = "$major.$minor.$patch"
if (suffix.isNotEmpty()) appVersionName += "-$suffix"

android {
    namespace = "ir.co.contact"
    compileSdk = 35

    defaultConfig {
        applicationId = "ir.co.contact"
        minSdk = 24
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            buildConfigField("boolean", "IS_DEBUG", "false")
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            buildConfigField("boolean", "IS_DEBUG", "true")
        }
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl

            val baseName = when (variant.buildType.name) {
                "debug" -> "Contact-debug"
                "release" -> "Contact-release"
                else -> "contact-${variant.buildType.name}"
            }

            output.outputFileName = "$baseName-${variant.versionName}-($appVersionCode).apk"
        }
    }

    tasks.withType<JavaCompile> {
        options.isIncremental = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material2)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.viewmodel.compose)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Splash
    implementation(libs.androidx.core.splashscreen)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    // Moshi
    implementation(libs.moshi)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    ksp(libs.moshi.codegen)

    // chucker
    debugImplementation(libs.chuckerDebug)
    releaseImplementation(libs.chuckerRelease)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)

    // Google font
    implementation(libs.androidx.ui.text.google.fonts)

    // Glide
    implementation(libs.compose.glide)

    // Credentials
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.material.icons.extended)

    // Swap Refresh
    implementation(libs.accompanist.swiperefresh)

    // Timber
    implementation(libs.timber)

    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Paging3
//    implementation(libs.androidx.paging.runtime.ktx)
//    implementation(libs.androidx.paging.compose)
}
