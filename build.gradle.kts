plugins {
    id("java")
    id("application")
    alias(libs.plugins.shadow)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

application.mainClass.set("org.wallentines.mcdl.Main")

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven("https://maven.wallentines.org/releases")
    maven("https://libraries.minecraft.net/")
}

configurations.shadow {
    extendsFrom(configurations.implementation.get())
}

tasks.jar {
    archiveClassifier.set("partial")
}

tasks.shadowJar {
    archiveClassifier.set("")
}

dependencies {

    implementation(libs.midnight.cfg)
    implementation(libs.midnight.cfg.json)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)
    compileOnly(libs.jetbrains.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}

java {
    manifest {
        attributes(Pair("Main-Class", application.mainClass))
    }
}

tasks.withType<JavaExec> {

    workingDir = File("run")
}
