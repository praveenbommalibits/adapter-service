plugins {
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.0"
    java
}

group = "org.adcb.adapter"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":adapter-spi"))
    implementation(project(":adapter-commons"))
    implementation(project(":adapter-transform-core"))

    implementation("org.springframework.boot:spring-boot-starter-webflux") // For WebClient
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

// Disable bootJar because no main class in this module
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

// Enable standard jar task
tasks.jar {
    enabled = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}
