plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.allopen") version "2.3.21" apply false
    id("com.google.devtools.ksp") version "2.3.7" apply false
    id("io.micronaut.application") version "4.6.2" apply false
    id("io.micronaut.aot") version "4.6.2" apply false
    id("org.sonarqube") version "7.3.1.8318"
    jacoco
}

allprojects {
    group = "com.ryabov.sentinelai"
    version = "0.1.0"
}

sonar {
    properties {
        property("sonar.projectKey", "sentinel-ai-platform")
        property("sonar.projectName", "Sentinel AI Platform")
        property(
            "sonar.host.url",
            providers.gradleProperty("sonarHostUrl")
                .orElse(providers.environmentVariable("SONAR_HOST_URL"))
                .orElse("http://localhost:9000")
                .get()
        )
        property("sonar.sourceEncoding", "UTF-8")
    }
}
