plugins {
    id("net.neoforged.gradle.userdev") version "7.1.36"
    kotlin("jvm") version "2.1.0"
    id("maven-publish")
}

version = property("mod_version") as String
group = property("maven_group") as String

base {
    archivesName.set(property("archives_base_name") as String)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

repositories {
    mavenCentral()
    google()
}

minecraft {
    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
        }
        create("client") { client() }
        create("server") { server() }
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${property("neo_version")}")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // Bundle Kotlin stdlib into the mod JAR
    jarJar("org.jetbrains.kotlin:kotlin-stdlib:${property("kotlin_version")}")
}

tasks.jar {
    manifest {
        attributes(
            "Specification-Title" to "Manosaba",
            "Specification-Vendor" to "Shiiyuko, Richie-liu111",
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "MixinConfigs" to "manosaba.mixins.json"
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

val minecraftVersion = property("minecraft_version") as String
val neoVersion = property("neo_version") as String

tasks.processResources {
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version, "minecraft_version" to minecraftVersion, "neo_version" to neoVersion)
    }
}
