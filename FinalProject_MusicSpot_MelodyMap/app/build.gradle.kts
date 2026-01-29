plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    /*ROOM dependency 관련 정보 추가*/
    id("com.google.devtools.ksp")
}

android {
    namespace = "ddwu.com.mobile.finalproject"
    compileSdk = 36

    defaultConfig {
        applicationId = "ddwu.com.mobile.finalproject"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }

    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Google play service 위치 관련 정보 추가
    implementation ("com.google.android.gms:play-services-location:21.3.0")

    // GoogleMap 관련 정보 추가
    implementation ("com.google.android.gms:play-services-maps:19.2.0")

    // 2. Room DB (MP2, MP3 활용)
    val room_version = "2.7.2"
    implementation("androidx.room:room-runtime:${room_version}")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:${room_version}") // 코루틴 지원용

    // 3. Glide (MP3 활용)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // 4. Retrofit (음악 검색 API - 새로 추가)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.cardview:cardview:1.0.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
}