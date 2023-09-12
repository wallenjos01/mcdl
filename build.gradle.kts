plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.wallentines"
version = "1.0-SNAPSHOT"

application.mainClass.set("org.wallentines.mcdl.Main")

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

configurations.create("shade").setExtendsFrom(listOf(configurations.getByName("implementation")))

repositories {
    mavenCentral()
    maven("https://maven.wallentines.org/")
    maven("https://libraries.minecraft.net/")
    mavenLocal()
}

dependencies {

    implementation("org.wallentines:midnightcfg-api:2.0.0-SNAPSHOT")
    implementation("org.wallentines:midnightcfg-codec-json:2.0.0-SNAPSHOT")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
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
