import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*  // for configure and create

plugins {
    java
}

group = "org.adcb"
version = project.findProperty("version") as String? ?: "1.0.0-SNAPSHOT"

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

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // Only configure publishing for specific modules
    if (project.name in listOf("adapter-client-starter", "adapter-commons", "adapter-transform-core")) {
        apply(plugin = "maven-publish")

        // Configure the publishing extension once the plugin is present
        plugins.withId("maven-publish") {
            extensions.configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("maven") {
                        from(components["java"])
                        artifactId = project.name

                        pom {
                            name.set("ADCB Adapter Platform - ${project.name}")
                            description.set(getModuleDescription(project.name))
                            url.set("https://github.com/${System.getenv("GITHUB_REPOSITORY") ?: "YOUR_USERNAME/YOUR_REPO"}")

                            licenses {
                                license {
                                    name.set("MIT License")
                                    url.set("https://opensource.org/licenses/MIT")
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
                                connection.set("scm:git:git://github.com/${System.getenv("GITHUB_REPOSITORY") ?: "YOUR_USERNAME/YOUR_REPO"}.git")
                                developerConnection.set("scm:git:ssh://github.com:${System.getenv("GITHUB_REPOSITORY") ?: "YOUR_USERNAME/YOUR_REPO"}.git")
                                url.set("https://github.com/${System.getenv("GITHUB_REPOSITORY") ?: "YOUR_USERNAME/YOUR_REPO"}")
                            }
                        }
                    }
                }

                repositories {
                    maven {
                        name = "GitHubPackages"
                        url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY") ?: "YOUR_USERNAME/YOUR_REPO"}")
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

// Helper for module descriptions
fun getModuleDescription(moduleName: String) = when (moduleName) {
    "adapter-client-starter" -> "Spring Boot starter for ADCB Adapter Platform - integration entry point"
    "adapter-commons"        -> "Common DTOs, models and utilities for ADCB"
    "adapter-transform-core" -> "Core transformation engine for requests/responses"
    else                     -> "ADCB Adapter Platform module"
}

// Aggregate publish task
tasks.register("publishClientModules") {
    group = "publishing"
    description = "Publish client-facing modules to GitHub Packages"
    dependsOn(
        ":adapter-client-starter:publish",
        ":adapter-commons:publish",
        ":adapter-transform-core:publish"
    )
}
