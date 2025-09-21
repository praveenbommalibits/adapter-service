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
    implementation(project(":adapter-protocol-rest"))
    implementation(project(":adapter-protocol-soap"))
    implementation(project(":adapter-commons"))
    implementation(project(":adapter-transform-core"))

    // Spring Boot WebFlux for reactive REST endpoints
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Jackson databind + YAML for JSON and YAML processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    // https://mvnrepository.com/artifact/io.github.resilience4j/resilience4j-circuitbreaker
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.3.0")
    implementation("io.github.resilience4j:resilience4j-retry:2.1.0")


    // Spring Boot Configuration Processor for @ConfigurationProperties
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Project Lombok for cleaner Java code (optional)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test dependencies
    testImplementation(project(":adapter-client-starter"))
    testImplementation(project(":adapter-transform-core"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
    testImplementation("javax.servlet:javax.servlet-api:4.0.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
