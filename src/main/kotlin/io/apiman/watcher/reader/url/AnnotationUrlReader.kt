package io.apiman.watcher.reader.url

import io.apiman.watcher.WatcherConfiguration
import io.apiman.watcher.appConfig
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

    private fun readHost() = resource.metadata.name!!

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