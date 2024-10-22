import buildlogic.Utils

plugins {
    id("build.common")
    id("com.github.johnrengelman.shadow")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
    }
}

tasks.shadowJar {
    archiveBaseName.set(Utils.getArchiveName(project, rootProject))
    archiveClassifier.set("")
}

tasks.jar {
    archiveClassifier.set("partial")
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
    skip()
}