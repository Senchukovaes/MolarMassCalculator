plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.demo_chem_calc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.demo_chem_calc"
//        minSdk = 24
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Базовые модули CDK
    implementation("org.openscience.cdk:cdk-core:2.8")
    implementation("org.openscience.cdk:cdk-data:2.8")
    implementation("org.openscience.cdk:cdk-interfaces:2.8")
    implementation("org.openscience.cdk:cdk-io:2.8") // для чтения/записи форматов

    // Дополнительные по необходимости
    implementation("org.openscience.cdk:cdk-formula:2.8") // формулы
    implementation("org.openscience.cdk:cdk-smiles:2.8") // SMILES
    implementation("org.openscience.cdk:cdk-inchi:2.8") // InChI
//    implementation("org.openscience.cdk:cdk-bundle:2.8")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}