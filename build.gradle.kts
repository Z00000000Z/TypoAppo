buildscript {
    val kotlinVersion by extra("1.3.72")
    val kotlinCoroutinesVersion by extra("1.3.7")
    val gsonVersion by extra("2.8.6")

    repositories {
        mavenCentral()
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

configure(subprojects) {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}
