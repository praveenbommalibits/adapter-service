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
}

tasks.jar {
    enabled = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}
