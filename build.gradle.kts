// root build.gradle.kts

plugins {
    java
}

// Helper for module descriptions
fun getModuleDescription(moduleName: String) = when (moduleName) {
    "adapter-client-starter" -> "Spring Boot starter for ADCB Adapter Platform - integration entry point"
    "adapter-commons"        -> "Common DTOs, models and utilities for ADCB"
    "adapter-transform-core" -> "Core transformation engine for requests/responses"
    "adapter-gateway-service"-> "Central orchestration engine for ADCB Adapter Platform"
    "adapter-protocol-rest"  -> "REST/JSON protocol handler implementation"
    "adapter-protocol-soap"  -> "SOAP/XML protocol handler implementation"
    "adapter-spi"            -> "Service Provider Interface for protocol handlers"
    else                     -> "ADCB Adapter Platform module"
}

group = "org.adcb.adapter"
// Version provided via -Pversion or default to SNAPSHOT
version = project.findProperty("version") as String? ?: "1.0.0-SNAPSHOT"

subprojects {
    apply(plugin = "java-library")         // use java-library for all modules

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // Configure publishing for all adapter modules
    if (project.name in listOf(
            "adapter-client-starter",
            "adapter-commons",
            "adapter-transform-core",
            "adapter-gateway-service",
            "adapter-protocol-rest",
            "adapter-protocol-soap",
            "adapter-spi"
        )) {
        apply(plugin = "maven-publish")

        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    artifactId = project.name

                    pom {
                        name.set("ADCB Adapter Platform - ${project.name}")
                        description.set(getModuleDescription(project.name))
                        url.set("https://github.com/praveenbommalibits/adapter-service")
                        licenses {
                            license {
                                name.set("Apache License 2.0")
                                url.set("https://opensource.org/licenses/Apache-2.0")
                            }
                        }
                        developers {
                            developer {
                                id.set("adcb-dev")
                                name.set("ADCB Development Team")
                                email.set("dev@adcb.com")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/praveenbommalibits/adapter-service.git")
                            developerConnection.set("scm:git:ssh://github.com/praveenbommalibits/adapter-service.git")
                            url.set("https://github.com/praveenbommalibits/adapter-service")
                        }
                    }
                }
            }
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/praveenbommalibits/adapter-service")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}

// Aggregate publish task in root project
tasks.register("publishClientModules") {
    group = "publishing"
    description = "Publish all adapter modules to GitHub Packages"
    dependsOn(
        ":adapter-client-starter:publishMavenPublicationToGitHubPackagesRepository",
        ":adapter-commons:publishMavenPublicationToGitHubPackagesRepository",
        ":adapter-transform-core:publishMavenPublicationToGitHubPackagesRepository",
        ":adapter-gateway-service:publishMavenPublicationToGitHubPackagesRepository",
        ":adapter-protocol-rest:publishMavenPublicationToGitHubPackagesRepository",
        ":adapter-protocol-soap:publishMavenPublicationToGitHubPackagesRepository",
        ":adapter-spi:publishMavenPublicationToGitHubPackagesRepository"
    )
}

// Fat jar creation tasks
tasks.register("buildGatewayFatJar") {
    group = "build"
    description = "Build fat jar for adapter-gateway-service"
    dependsOn(":adapter-gateway-service:bootJar")
}

tasks.register("buildClientStarterFatJar") {
    group = "build"
    description = "Build fat jar for adapter-client-starter"
    dependsOn(":adapter-client-starter:bootJar")
}
