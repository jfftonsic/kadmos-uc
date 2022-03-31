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
    mavenLocal()
    mavenCentral()
}

tasks.getByName<JavaCompile>("compileJava") {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

fun JavaForkOptions.prepareJvmArgs(): Unit {

    val jvmArgs2 = if (project.hasProperty("jvmArgs"))
        project.property("jvmArgs").toString().split("\\s+".toRegex()).toMutableList()
    else emptyList<String>().toMutableList()

    // using graalvm this was not needed
//    if (project.hasProperty("flightRecorder") && project.property("flightRecorder").toString().toBoolean()) {
        // the following does not exist in graalvm: -XX:+UnlockCommercialFeatures
        // jvmArgs2.addAll(listOf("-XX:+FlightRecorder"))
//    }

    val hasPropertyFlightRecorderFile = project.hasProperty("flightRecorderFile")
    val hasPropertyFlightRecorderTemplate = project.hasProperty("flightRecorderTemplate")
    val flightRecorderFile = if (hasPropertyFlightRecorderFile) project.property("flightRecorderFile").toString() else ""
    val flightRecorderTemplate = if (hasPropertyFlightRecorderTemplate) project.property("flightRecorderTemplate").toString() else ""
    if (hasPropertyFlightRecorderFile) {
        jvmArgs2.add("-XX:StartFlightRecording=filename=$flightRecorderFile${if (hasPropertyFlightRecorderTemplate) ",settings=$flightRecorderTemplate" else ""}")
    }

    if (project.hasProperty("remoteJmx")) {
        val remoteJmxPort = project.property("remoteJmx").toString().trim()
        jvmArgs2.addAll(
            listOf(
                "-Dcom.sun.management.jmxremote=true",
                "-Dcom.sun.management.jmxremote.port=$remoteJmxPort",
                "-Dcom.sun.management.jmxremote.authenticate=false",
                "-Dcom.sun.management.jmxremote.ssl=false"
            )
        )
    }

    if (project.hasProperty("remoteDebug")) {
        val remoteDebugPort = project.property("remoteDebug").toString().trim()
        val suspend = if (project.hasProperty("remoteDebugSuspend") && project.property("remoteDebugSuspend").toString().toBoolean()) "y" else "n"
        jvmArgs2.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=$suspend,address=*:$remoteDebugPort")
    }

//    println("The JVM args for the execution will be: ${jvmArgs2.joinToString(" ")}")

    jvmArgs = jvmArgs2


}

tasks.test {
    testLogging.showExceptions = true
    useJUnitPlatform()
    prepareJvmArgs()
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
    prepareJvmArgs()
//    jvmArgs = if (project.hasProperty("jvmArgs"))
//        project.property("jvmArgs").toString().split("\\s+".toRegex())
//    else emptyList()
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

tasks.withType<Test> {

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