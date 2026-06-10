import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.SourceSetContainer

plugins {
    id("org.springframework.boot") version "3.2.4" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.spring") version "1.9.24" apply false
    kotlin("plugin.jpa") version "1.9.24" apply false
}

allprojects {
    group = "com.circleguard"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        "implementation"(platform("org.springframework.boot:spring-boot-dependencies:3.2.4"))
        "testImplementation"(platform("org.springframework.boot:spring-boot-dependencies:3.2.4"))
        "compileOnly"("org.projectlombok:lombok")
        "annotationProcessor"("org.projectlombok:lombok")
        "testCompileOnly"("org.projectlombok:lombok")
        "testAnnotationProcessor"("org.projectlombok:lombok")
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testRuntimeOnly"("com.h2database:h2")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }

    apply(plugin = "jacoco")

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    tasks.withType<JacocoReport> {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    // Add a dedicated e2eTest task per subproject that only runs classes matching *E2ETest
    tasks.register<Test>("e2eTest") {
        description = "Runs E2E tests in this project (classes matching *E2ETest)"
        group = "verification"
        useJUnitPlatform()
        // Only run if there are compiled test classes matching the E2E pattern
        onlyIf {
            try {
                val sourceSets = extensions.getByType(SourceSetContainer::class.java)
                val classesDirs = sourceSets.getByName("test").output.classesDirs
                classesDirs.any { dir ->
                    fileTree(dir).matching { include("**/*E2ETest*.class") }.files.isNotEmpty()
                }
            } catch (e: Exception) {
                false
            }
        }
        filter {
            includeTestsMatching("*E2ETest")
        }
    }
}

// Global JaCoCo aggregation task
tasks.register<JacocoReport>("jacocoRootReport") {
    group = "verification"
    description = "Generates an aggregate report from all subprojects"
    dependsOn(subprojects.map { it.tasks.matching { t -> t.name == "jacocoTestReport" } })

    val executionDataFiles = files(subprojects.map { 
        layout.buildDirectory.dir("jacoco").map { d -> d.file("test.exec") }
    })
    
    executionData.setFrom(executionDataFiles)
    
    subprojects.forEach { proj ->
        proj.plugins.withType<JavaPlugin> {
            val sourceSets = proj.extensions.getByType<SourceSetContainer>()
            sourceSets.getByName("main").allJava.srcDirs.forEach { srcDir ->
                sourceDirectories.from(srcDir)
            }
            sourceSets.getByName("main").output.classesDirs.forEach { classDir ->
                classDirectories.from(classDir)
            }
        }
    }

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/root"))
    }
}

// Aggregate e2eTest across all subprojects from the root project
tasks.register("e2eTest") {
    group = "verification"
    description = "Run all e2eTest tasks in subprojects"
    // Ensure the task depends on each subproject's :<project>:e2eTest
    subprojects.forEach { proj ->
        dependsOn("${proj.path}:e2eTest")
    }
}
