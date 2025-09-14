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
    implementation(project(":adapter-commons"))
    implementation(project(":adapter-spi"))

    // JSON template and path extraction
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.jayway.jsonpath:json-path:2.8.0")

    // XML/SOAP template and path extraction
    implementation("org.freemarker:freemarker:2.3.32")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")
    //implementation("javax.xml.xpath:jaxp-api:1.4.0")

    compileOnly("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.17.0")


    // Utility and logging
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.slf4j:slf4j-api:2.0.13")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
