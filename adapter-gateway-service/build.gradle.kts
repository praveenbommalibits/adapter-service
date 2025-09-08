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
    implementation(project(":adapter-commons"))

    // Spring Boot WebFlux for reactive REST endpoints
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Jackson databind + YAML for JSON and YAML processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    // Spring Boot Configuration Processor for @ConfigurationProperties
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Project Lombok for cleaner Java code (optional)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
