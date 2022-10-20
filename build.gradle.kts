/// build.gradle.kts (Jenkins shared library):
/// =========================================
///
/// Access gradle.properties:
///     yes -> "val prop_name = project.extra['prop.name']"
///     no  -> "val prop_name = property('prop.name')"

/** 1) Plugins used globally */
plugins {
    groovy
    jacoco

    id("org.sonarqube") version "3.4.0.2513"
}


/** 2) General information regarding the library */
group   = project.extra["library.group"]!! as String
version = project.extra["library.version"]!! as String


/** 3) Dependency source configuration */
repositories {
    mavenCentral()
    gradlePluginPortal()
}


/** 4) Library dependencies */
dependencies {
    implementation("org.codehaus.groovy:groovy-all:3.0.13")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}


/** 5) Source set configuration */
sourceSets {
    main.get().groovy.srcDirs("src", "vars")
    test.get().groovy.srcDir("test")
}


/** 6) JaCoCo configuration */
jacoco {
    toolVersion = "0.8.8"
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }

    // INFO: Excludes the files in "vars" as they only call methods in "src"!
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching { exclude("*.class") }
    )
}


/** 7) Gradle test configuration */
tasks.withType<Test> {
    ignoreFailures = true
    testLogging.showStandardStreams = true
}
