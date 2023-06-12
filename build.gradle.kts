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
    maven("https://libraries.minecraft.net/")
    mavenLocal()
}

dependencies {

    implementation("org.wallentines:midnightcfg:1.0-SNAPSHOT")

    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")

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