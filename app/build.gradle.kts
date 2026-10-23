
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.alnadhari.campus"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.alnadhari.campus"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf(
                    "androidx.**",
                    "kotlin.**",
                    "kotlinx.**"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Android basic libraries
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Cards for dashboard and task items
    implementation("androidx.cardview:cardview:1.0.0")

    // Task lists with Adapter and ViewHolder
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Unit testing
    testImplementation(libs.junit)

    // Android UI testing
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
