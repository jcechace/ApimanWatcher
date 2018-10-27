pluginManagement {

    repositories {
        jcenter()
        gradlePluginPortal()
        maven(url = "https://kotlin.bintray.com/kotlin-eap")
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}