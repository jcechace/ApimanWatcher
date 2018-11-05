package io.apiman.sidekick.reader.url

import io.apiman.sidekick.appConfig
import io.fabric8.kubernetes.api.model.Service
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url

/**
 * Reads api endpoint from k8 service annotations
 *
 * @author Jakub Cechacek
 */
class AnnotationUrlReader(private val resource: Service) {

    private val discoveryCfg = appConfig().discovery

    /**
     * Read api endpoint URL from k8 service annotations
     */
    fun readUrl(): Url {
        return URLBuilder(protocol = readProtocol(),host = readHost()).apply {
            path(readPath())
            readPort().isNotEmpty()
            if (readPort().isNotBlank()) port = readPort().toInt()
        }.build()
    }

    /**
     * Reads host from service name
     *
     * @return host part of api endpoint
     */
    private fun readHost(): String {
        val name = resource.metadata.name
        val org = resource.metadata.namespace
        return "${name}.${org}.svc"
    }

    /**
     * Reads port from annotation specified by [io.apiman.sidekick.AppConfig]
     *
     * @return port or empty String
     */
    private fun readPort() = readAnnotation(discoveryCfg.annotations.port,"")

    /**
     * Reads path from annotation specified by [io.apiman.sidekick.AppConfig]
     *
     * @return path part of api endpoint
     */
    private fun readPath() = readAnnotation(discoveryCfg.annotations.path, "")

    /**
     * Reads protocol from annotation specified by [io.apiman.sidekick.AppConfig]
     *
     * @return protocol part of api endpoint
     */
    private fun readProtocol(): URLProtocol {
        val scheme = readAnnotation(discoveryCfg.annotations.scheme, "http")
        return URLProtocol.createOrDefault(scheme)
    }

    private fun  readAnnotation(key: String, default: String): String {
        return resource.metadata?.annotations?.get(key) ?: default
    }
}