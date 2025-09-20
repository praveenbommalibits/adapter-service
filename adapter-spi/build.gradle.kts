plugins {
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
    implementation(project(":adapter-commons"))
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.5.5") // For WebClient

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.jar {
    enabled = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}
