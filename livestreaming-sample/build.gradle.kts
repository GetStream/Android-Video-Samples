plugins {
  id(libs.plugins.androidApplication.get().pluginId)
  id(libs.plugins.kotlinAndroid.get().pluginId)
  id(libs.plugins.spotless.get().pluginId)
  id(libs.plugins.kotlin.serialization.get().pluginId)
  id(libs.plugins.ksp.get().pluginId)
  id(libs.plugins.google.secrets.get().pluginId)
}

android {
  namespace = "io.getstream.android.samples.livestreaming"
  compileSdk = 34

  defaultConfig {
    applicationId = "io.getstream.android.samples.livestreaming"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "secrets.defaults.properties"
  }
}

dependencies {
  // Stream Video SDK
  implementation(libs.stream.video.ui.compose)
  implementation(libs.stream.video.ui.previewdata)

  // Compose
  implementation(platform(libs.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.material.iconsExtended)
  implementation(libs.androidx.lifecycle.runtime.compose)
}