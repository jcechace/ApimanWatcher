package io.apiman.sidekick.reader.url

import io.apiman.sidekick.appConfig
import io.fabric8.kubernetes.api.model.Service
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url

/**
 *
 * @author Jakub Cechacek
 */
class AnnotationUrlReader(val resource: Service) {

    private val discoveryCfg = appConfig().discovery

    fun readUrl(): Url {
        return URLBuilder(protocol = readProtocol(),host = readHost()).apply {
            path(readPath())
            readPort().isNotEmpty()
            if (readPort().isNotBlank()) port = readPort().toInt()
        }.build()
    }

    private fun readHost(): String {
        val name = resource.metadata.name
        val org = resource.metadata.namespace
        return "${name}.${org}.svc"
    }

    private fun readPort() = readAnnotation(discoveryCfg.annotations.port,"")

    private fun readPath() = readAnnotation(discoveryCfg.annotations.path, "")

    private fun readProtocol(): URLProtocol {
        val scheme = readAnnotation(discoveryCfg.annotations.scheme, "http")
        return URLProtocol.createOrDefault(scheme)
    }

    private fun  readAnnotation(key: String, default: String): String {
        return resource.metadata?.annotations?.get(key) ?: default
    }
}