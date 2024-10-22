plugins {
    id("build.application")
    id("build.library")
    id("build.shadow")
}

application.mainClass.set("org.wallentines.mcdl.Main")

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
}

dependencies {

    implementation(libs.midnight.cfg)
    implementation(libs.midnight.cfg.json)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)

    shadow(libs.midnight.cfg)
    shadow(libs.midnight.cfg.json)
    shadow(libs.slf4j.api)
    shadow(libs.slf4j.simple)
}