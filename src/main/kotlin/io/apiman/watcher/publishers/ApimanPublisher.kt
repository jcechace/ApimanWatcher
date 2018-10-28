package io.apiman.watcher.publishers

import io.apiman.gateway.engine.beans.Api
import io.apiman.watcher.WatcherConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import mu.KLogging
import java.io.IOException

class ApimanPublisher(private val client: HttpClient) : Publisher<Api> {
    companion object : KLogging()

    private suspend fun execute(block: suspend () -> Boolean): Boolean {
        return try {
            block()
        } catch (e: IOException) {
            logger.error { e }
            false
        }
    }

    override suspend fun retireApi(api: Api): Boolean = execute {
        logger.info { "Retiring API ${api.organizationId}:${api.apiId}" }
        client.call {
            method = HttpMethod.Delete
            url {
                host = WatcherConfiguration.APIMAN_SERVICE_HOST
                path(
                    "organizations", api.organizationId,
                    "apis", api.apiId,
                    "versions", api.version
                )
            }
        }.response.status.isSuccess()
    }

    override suspend fun publishApi(api: Api): Boolean = execute {
        logger.info { "Publishing API ${api.organizationId}:${api.apiId}" }
        client.call {
            url {
                host = WatcherConfiguration.APIMAN_SERVICE_HOST
                path("apis")
            }
            contentType(ContentType.Application.Json)
            body = api
            method = HttpMethod.Put
        }.response.status.isSuccess()
    }
}