plugins {
    id("net.neoforged.gradle.userdev") version "7.0.171"
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
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
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

minecraft {
    mappings {
        channel.set("official")
        version.set("1.21.1")
    }

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
        }

        create("client") {
            client()
        }

        create("server") {
            server()
        }
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${property("neo_version")}")

    // Mixin annotation processor
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // Compose Desktop (renders UI via Skia -> OpenGL FBO)
    implementation(compose.desktop.currentOs) {
        exclude(group = "org.jetbrains.compose.material")
    }
    implementation(compose.foundation)
    implementation(compose.ui)

    // Bundle Kotlin stdlib + coroutines into the mod JAR (NeoForge JarJar)
    jarJar(implementation("org.jetbrains.kotlin:kotlin-stdlib:${property("kotlin_version")}")!!)
    jarJar(implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")!!)
}

// Register Mixin configs via MANIFEST.MF (NeoForge reads this)
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

tasks.processResources {
    filesMatching("META-INF/mods.toml") {
        expand(
            "version" to project.version,
            "minecraft_version" to property("minecraft_version"),
            "neo_version" to property("neo_version")
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
    }
}
