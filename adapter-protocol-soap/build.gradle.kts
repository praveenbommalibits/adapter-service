plugins {
    //id("org.springframework.boot") version "3.5.5"
    //id("io.spring.dependency-management") version "1.1.0"
    //java
    `java-library`
}


group = "org.adcb.adapter"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":adapter-spi"))
    implementation(project(":adapter-commons"))
    implementation(project(":adapter-transform-core"))
    // For SOAP transport and XML processing
    implementation("org.springframework.ws:spring-ws-core:4.0.3")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.2.5")
    implementation("org.springframework.boot:spring-boot-starter-web-services:3.2.5")
    implementation("org.apache.commons:commons-lang3:3.14.0")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // XML serialization/deserialization
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")

    implementation("org.slf4j:slf4j-api:2.0.13")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.5")
}


tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jar {
    enabled = true
}
