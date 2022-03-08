buildscript {
    repositories {
        mavenCentral()
        maven("https://repo.spring.io/milestone")
        maven("https://repo.spring.io/snapshot")
    }
//    dependencies {
//        classpath("org.springframework.cloud:spring-cloud-contract-gradle-plugin:3.1.2-SNAPSHOT")
//    }
}

plugins {
    id("kadmos-uc.spring-conventions")
//    id("org.asciidoctor.convert") version "1.5.8"
}

//apply(plugin="spring-cloud-contract")

group = "com.example"
version = "1.0.0-SNAPSHOT"

//val snippetsDir by extra(file("build/generated-snippets"))
val springCloudVersion by extra("2021.0.2-SNAPSHOT")

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
    implementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner"){
        exclude(group="org.springframework.boot", module="spring-boot-starter-web")
    }

//    implementation("org.springframework.boot:spring-boot-starter-actuator")
//    implementation("org.springframework.boot:spring-boot-starter-validation")
//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.springframework.boot:spring-boot-starter-hateoas")
//    implementation("org.springframework.boot:spring-boot-starter-jooq")
//    implementation("org.liquibase:liquibase-core")
//    implementation("org.springframework.cloud:spring-cloud-sleuth-zipkin")
//    implementation("org.springframework.cloud:spring-cloud-starter")
//    implementation("org.springframework.cloud:spring-cloud-starter-consul-config")
//    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
//    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
//    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
//    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
//    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

//tasks.getByName<Test>("contractTest") {
//    useJUnitPlatform()
//}
//
//tasks.getByName<Test>("test") {
//    outputs.dir(snippetsDir)
//}