package io.apiman.sidekick

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract

data class AppConfig(
    val openshift: OpenShiftConfig,
    val discovery: DiscoveryConfig,
    val ssl: SSSLConfig?,
    val apiman: ApimanConfig
)

data class SSSLConfig(
    val path: String?,
    val password: String?
)

data class OpenShiftConfig(
    val url: String,
    val token: String?
)

data class DiscoveryConfig(
    val label: String,
    val annotations: DiscoveryAnnotationConfig
)

data class DiscoveryAnnotationConfig(
    val scheme: String,
    val path: String,
    val port: String,
    val descriptorPath: String
)

data class ApimanConfig(
    val host: String,
    val username: String,
    val password: String,
    val annotations: ApimanAnnotationConfig
)

data class ApimanAnnotationConfig(
    val policies: String
)

fun appConfig()  = ConfigFactory.load().extract<AppConfig>()