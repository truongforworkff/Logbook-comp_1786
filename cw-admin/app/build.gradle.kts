plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}
val syncProperties = java.util.Properties().apply {
    rootProject.file("sync.properties").takeIf { it.isFile }?.inputStream()?.use { load(it) }
}
val syncWriteKey = System.getenv("EVENTA_SYNC_WRITE_KEY") ?: syncProperties.getProperty("writeKey", "")
require(syncWriteKey.isEmpty() || syncWriteKey.matches(Regex("[0-9a-f]{64}"))) {
    "EVENTA_SYNC_WRITE_KEY must contain 64 lowercase hexadecimal characters."
}
android {
    namespace = "com.comp1786.room"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.comp1786.eventa.admin"
        buildConfigField("String", "CLOUD_WRITE_KEY", "\"$syncWriteKey\"")
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        javaCompileOptions { annotationProcessorOptions { arguments["room.schemaLocation"] = "$projectDir/schemas" } }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { viewBinding = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
}
dependencies {
    implementation("androidx.work:work-runtime:2.10.5")
    // Firestore supplies Guava at runtime; Worker also needs its future API at compile time.
    implementation("com.google.guava:guava:33.4.8-android")
    // Firebase 34.x supports this Java project's AGP 8.13 and Kotlin metadata reader.
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core:1.17.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.4")
    implementation("androidx.lifecycle:lifecycle-livedata:2.9.4")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    testImplementation("junit:junit:4.13.2")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.room:room-runtime:2.8.5")
    annotationProcessor("androidx.room:room-compiler:2.8.5")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.room:room-testing:2.8.5")
}
