plugins {
    id("java-library")
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
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.2.5")
    implementation("org.springframework.boot:spring-boot-starter")
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
