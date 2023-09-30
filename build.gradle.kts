plugins {
    alias(libs.plugins.simple.kotlin.multiplatform.plugin)
}

group = "io.github.edmondantes"

kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

licenses {
    apache2()
}

developers {
    developer {
        name = "Ilia Loginov"
        email = "masaqaz40@gmail.com"
        organizationName("github")
        role("Maintainer")
        role("Developer")
    }
}

simplePom {
    any {
        title = "Simple kotlinx serialization utils"
        description = "Small library which provide some utilities for koltinx.serialization"
        url = "#github::Simple-Kotlin-Project::${project.name}"
        scm {
            url = "#github::Simple-Kotlin-Project::${project.name}::master"
            connection = "#github::Simple-Kotlin-Project::${project.name}"
            developerConnection = "#github::Simple-Kotlin-Project::${project.name}"
        }
    }
}