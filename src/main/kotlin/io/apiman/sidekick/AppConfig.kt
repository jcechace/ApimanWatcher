package io.apiman.sidekick

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.takeFrom

data class AppConfig(
    val openshift: OpenShiftConfig,
    val discovery: DiscoveryConfig,
    val ssl: SSLConfig,
    val apiman: ApimanConfig
)

data class SSLConfig(
    val trustStore: SSLTrustStoreConfig?,
    val trustAll: Boolean
)

data class SSLTrustStoreConfig(
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
    val scheme: String,
    val host: String,
    val username: String,
    val password: String,
    val annotations: ApimanAnnotationConfig
) {
    val url = URLBuilder().apply {
        protocol = URLProtocol.createOrDefault(scheme)
        host = this@ApimanConfig.host
    }
}

data class ApimanAnnotationConfig(
    val policies: String
)

fun appConfig() = ConfigFactory.load().extract<AppConfig>()

/**
 * Same as [HttpRequestBuilder.url] with preset default to Apiman's gateway api
 */
fun HttpRequestBuilder.apimanUrl(block: URLBuilder.(URLBuilder) -> Unit) {
    url.takeFrom(appConfig().apiman.url)
    url.block(url)
}
