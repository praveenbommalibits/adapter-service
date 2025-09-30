plugins {
    java
}

// At very top of build.gradle.kts
fun getModuleDescription(moduleName: String) = when (moduleName) {
    "adapter-client-starter" -> "Spring Boot starter for ADCB Adapter Platform - integration entry point"
    "adapter-commons"        -> "Common DTOs, models and utilities for ADCB"
    "adapter-transform-core" -> "Core transformation engine for requests/responses"
    else                     -> "ADCB Adapter Platform module"
}


group = "org.adcb"
// Use the version from command line or default to SNAPSHOT
version = project.findProperty("version") ?: "1.0.0-SNAPSHOT"

subprojects {
    apply(plugin = "java")

    group = "org.adcb.adapter"  // Change this to adapter group
    version = rootProject.version  // Inherit from root

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // Only configure publishing for specific modules
    if (project.name in listOf("adapter-client-starter", "adapter-commons", "adapter-transform-core")) {
        apply(plugin = "maven-publish")

        plugins.withId("maven-publish") {
            extensions.configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("maven") {
                        from(components["java"])

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
}
