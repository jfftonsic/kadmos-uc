pluginManagement {
    repositories {
        maven("https://repo.spring.io/milestone")
        maven("https://repo.spring.io/snapshot")
        gradlePluginPortal()
    }
}



rootProject.name = "kadmos-uc"
include("savings-a")
include("api-gateway")
include("transference-svc")
include("swagger-svc")
