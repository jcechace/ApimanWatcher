import org.jetbrains.kotlin.gradle.dsl.Coroutines

val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val kotlin_logging_version: String by project
val slf4j_version: String by project

plugins {
    application
    kotlin("jvm") version "1.3.0-rc-190"
}

application {
    mainClassName = "io.apiman.watcher.WatcherAppKt"
}


repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://kotlin.bintray.com/kotlin-eap") }
}

dependencies {
    compile(kotlin("stdlib", kotlin_version))

//      http client
    compile("io.ktor:ktor-client-core:$ktor_version")
    compile("io.ktor:ktor-client-core-jvm:$ktor_version")
    compile("io.ktor:ktor-client-okhttp:$ktor_version")
    compile("io.ktor:ktor-client-auth-basic:$ktor_version")
    compile("io.ktor:ktor-client-json-jvm:$ktor_version")
    compile("io.ktor:ktor-client-gson:$ktor_version")

//      json library
    compile("com.google.code.gson:gson:2.8.5")

//      logging
    compile ("io.github.microutils:kotlin-logging:$kotlin_logging_version")
    compile("org.slf4j:slf4j-api:$slf4j_version")
    compile("ch.qos.logback:logback-classic:$logback_version")

//      k8 client
    compile("io.fabric8:kubernetes-client:4.1.0")
    compile("io.fabric8:kubernetes-model:4.1.0")
    compile("io.fabric8:openshift-client:4.1.0")

//      apiman dependencies
    compile("io.apiman:apiman-gateway-engine-beans:1.5.1.Final")
}

