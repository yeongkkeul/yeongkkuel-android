import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}


val localProperties = Properties()
localProperties.load(project.rootProject.file("local.properties").inputStream())
val kakaoApiKey = localProperties.getProperty("kakao_NATIVE_APP_KEY")?:""
val nativeAppKey = localProperties.getProperty("kakao_NATIVE_APP_KEY_MANIFEST")?:""

android {
    namespace = "com.example.yeongkkuel"
    compileSdk = 35

    defaultConfig {
        buildConfigField("String", "kakao_NATIVE_APP_KEY", "\"$kakaoApiKey\"")
        manifestPlaceholders["NATIVE_APP_KEY"] = nativeAppKey

        applicationId = "com.example.yeongkkuel"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {

    //Google Credential Manager
    implementation("androidx.credentials:credentials:1.3.0")

    // optional - needed for credentials support from play services, for devices running
    // Android 13 and below.
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.0 ")
    implementation ("androidx.core:core-splashscreen:1.0.1") //splash Theme 적용
    implementation ("com.kakao.sdk:v2-all:2.20.6") // 전체 모듈 설치, 2.11.0 버전부터 지원
    implementation ("com.kakao.sdk:v2-user:2.20.6") // 카카오 로그인 API 모듈
    implementation ("com.kakao.sdk:v2-cert:2.20.6") // 카카오톡 인증 서비스 API 모듈
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.timber)

    // Pie chart(원형 차트)
    implementation(libs.mpandroidchart)

    // Gson
    implementation(libs.gson)

    // ActivityViewModels
    implementation(libs.androidx.activity.ktx)
}