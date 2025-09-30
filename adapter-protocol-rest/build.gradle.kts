plugins {
    //id("org.springframework.boot") version "3.5.5"
    //id("io.spring.dependency-management") version "1.1.0"
    //java
    `java-library`
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

    implementation("org.springframework.boot:spring-boot-starter-webflux:3.5.5") // For WebClient
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.2")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.5")
    testImplementation("io.projectreactor:reactor-test:3.5.14")
}

// Disable bootJar because no main class in this module
/*tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}*/

// Enable standard jar task
tasks.jar {
    enabled = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}
