plugins {
    java
    checkstyle
    application

    id("io.freefair.lombok")

    // NOTE: external plugin version is specified in implementation dependency
    // artifact of the project's build file
    id("com.github.spotbugs")

    idea
}

val javaVersion by extra(JavaVersion.VERSION_17.toString())
val junitJupiterVersion by extra("5.8.0")

repositories {
    mavenCentral()
}

tasks.getByName<JavaCompile>("compileJava") {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.test {
    testLogging.showExceptions = true
    useJUnitPlatform()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// If you are using java-application plugin, use the section below to add a way to set jvm args for the
// child process that gradle runs for your application.
// usage example: ./gradlew run -PjvmArgs="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:9999"
tasks.getByName<JavaExec>("run") {
    jvmArgs = if (project.hasProperty("jvmArgs"))
        project.property("jvmArgs").toString().split("\\s+".toRegex())
    else emptyList()
}

idea {
    module {

        excludeDirs.add(file("target"))
        excludeDirs.add(file("out"))
        excludeDirs.add(file("gradle"))
        excludeDirs.add(file("docker"))
        // if you are using vaadin, don't forget to exclude the following
        // otherwise intellij will try to index a LOT of things without need. (takes more than 30 mins)
        // if not using vaadin, you can also leave this here without problem.
        // vaadin - start
        excludeDirs.add(file("frontend"))
        excludeDirs.add(file("node_modules"))
        // vaadin - end
    }
}

tasks.getByName<Test>("test") {
    testLogging.showExceptions = true
    useJUnitPlatform()
}

configurations {
    implementation {
        resolutionStrategy {
            failOnVersionConflict()
        }
    }
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}