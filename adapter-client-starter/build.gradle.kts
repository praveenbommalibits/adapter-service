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

    implementation(project(":adapter-gateway-service"))
    implementation(project(":adapter-commons"))
    implementation(project(":adapter-spi"))

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
