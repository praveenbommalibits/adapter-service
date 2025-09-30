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
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.5"))

    // Spring Boot WebFlux for reactive REST endpoints
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    api(project(":adapter-gateway-service"))
    api(project(":adapter-transform-core"))
    api(project(":adapter-commons"))
    api(project(":adapter-spi"))

    implementation("org.springframework.boot:spring-boot-starter")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}


tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jar {
    enabled = true
}
