package io.apiman.sidekick.publishers

import io.apiman.gateway.engine.beans.Api
import io.apiman.sidekick.apimanUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.get
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KLogging
import java.io.IOException


class ApimanPublisher(private val client: HttpClient) : ApiPublisher<Api> {
    companion object : KLogging()

    private suspend fun execute(block: suspend () -> Boolean): Boolean {
        return try {
            block()
        } catch (e: IOException) {
            logger.error { e }
            false
        }
    }

    private suspend fun fetchOrganizations(): List<String> {
        logger.debug { "Fetching ORGs" }
        return client.get {
            apimanUrl {
                path("organizations")
            }
        }
    }

    private suspend fun fetchPublishedApis(org: String): List<String> {
        logger.debug { "Fetching API for $org" }
        return client.get {
            apimanUrl {
                path("organizations", org, "apis")
            }
        }
    }

    private suspend fun fetchPublishedApiVersions(org: String, api: String): List<Api> {
        logger.debug { "Fetching VERSIONS for $org:$api" }
        val list = client.get<List<String>> {
            apimanUrl {
                path("organizations", org, "apis", api, "versions")
            }
        }

        return list.map {
            Api().apply {
                organizationId = org
                apiId = api
                version = it
            }
        }
    }

    override suspend fun fetchPublished(): List<Api> = coroutineScope {
        fetchOrganizations() .map { org ->
            async {
                fetchPublishedApis(org).map { api ->
                    async {
                        fetchPublishedApiVersions(org, api)
                    }
                }.flatMap { it.await() }
            }
        }.flatMap { it.await() }
    }

    fun URLBuilder.apiman(block: URLBuilder.(URLBuilder) -> Unit): Unit {
        block(this)
    }


    override suspend fun retireApi(api: Api): Boolean = execute {
        logger.info { "Retiring API ${api.organizationId}:${api.apiId}" }
        client.call {
            method = HttpMethod.Delete
            apimanUrl {
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
            apimanUrl {
                path("apis")
            }
            contentType(ContentType.Application.Json)
            body = api
            method = HttpMethod.Put
        }.response.status.isSuccess()
    }
}