rootProject.name = "simple-kotlin-multipart"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./gradle/libs.version.toml"))
        }
    }
}

pluginManagement {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
        gradlePluginPortal()
    }
}