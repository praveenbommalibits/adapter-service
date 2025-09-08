plugins {
    // Java plugin only for root project (if needed)
    java
}

group = "org.adcb"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    // Dependency management via Spring BOM for consistency
    dependencies {
        // Align JUnit versions for all subprojects
        "testImplementation"(platform("org.junit:junit-bom:5.10.0"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Optional: Define task dependencies or aggregate tasks if needed
