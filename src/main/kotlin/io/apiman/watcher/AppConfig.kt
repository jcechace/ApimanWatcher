package io.apiman.watcher

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract

data class AppConfig(
    val openshift: OpenShiftConfig,
    val discovery: DiscoveryConfig
)

data class OpenShiftConfig(
    val url: String,
    val token: String
)

data class DiscoveryConfig(
    val label: String,
    val annotations: DiscoveryAnnotationConfig,
    val apiman: DiscoveryApimanConfig
)

data class DiscoveryAnnotationConfig(
    val scheme: String,
    val path: String,
    val port: String,
    val descriptorPath: String
)

data class DiscoveryApimanConfig(
    val host: String,
    val annotations: DiscoveryApimanAnnotationConfig
)

data class DiscoveryApimanAnnotationConfig(
    val policies: String
)

fun appConfig()  = ConfigFactory.load().extract<AppConfig>()