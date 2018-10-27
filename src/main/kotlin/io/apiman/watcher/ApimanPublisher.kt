package io.apiman.watcher

import io.apiman.gateway.engine.beans.Api
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import java.io.IOException

class ApimanPublisher(val client: HttpClient) : Publisher<Api> {
    override suspend fun retireApi(api: Api): Boolean {
        try {
            println("in retire #1")
            val response = client.call {
                method = HttpMethod.Delete
                url {
                    host = "apiman-api-jcechace-playground.apps.ocp.api-qe.eng.rdu2.redhat.com"
                    path(
                        "organizations", api.organizationId,
                        "apis", api.apiId,
                        "versions", api.version)
                }
                println(url.build())
            }.response

            response.use {
                println("in retire #2 / response code ${it.status}")
            }

        } catch (e : IOException) {
            System.err.println("in retire #4 / error ${e::class}")
        }
        println("in retire #3 / the end")
        return true
    }

    override suspend fun publishApi(api: Api): Boolean {
        try {
            println("in publish #1")
            val response = client.call {
                url("http://apiman-api-jcechace-playground.apps.ocp.api-qe.eng.rdu2.redhat.com/apis")
                contentType(ContentType.Application.Json)
                body = api
                method = HttpMethod.Put
            }.response

            response.use {
                println("in publish #2 / response code ${response.status}")
            }

        } catch (e : IOException) {
            System.err.println("in publish #4 / error ${e::class}")
        }
        println("in publish #3 / the end")
        return true

    }
}