package io.apiman.sidekick.publishers

import io.apiman.gateway.engine.beans.Api
import io.apiman.sidekick.apimanUrl
import io.apiman.sidekick.publishers.ApimanPublisher
import io.apiman.sidekick.reader.url.AnnotationUrlReader
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.http.Url
import io.mockk.*
import kotlinx.coroutines.runBlocking

class ApimanPublisherTest :FreeSpec({

    "ApimanPublisher" - {
        val client = mockk<HttpClient>()
        val publisher = spyk(ApimanPublisher(client), recordPrivateCalls = true)

        // Organizations
        coEvery { publisher["fetchOrganizations"]() } returns listOf("org1", "org2")

        // Org1
        coEvery { publisher invoke "fetchPublishedApis" withArguments listOf("org1")} returns listOf("api1", "api2")

        // Org2
        coEvery { publisher invoke "fetchPublishedApis" withArguments listOf("org2")} returns listOf("api1", "api3")

        coEvery { publisher invoke "fetchPublishedApiVersions" withArguments listOf("org1", "api1")} returns listOf(Api())
        coEvery { publisher invoke "fetchPublishedApiVersions" withArguments listOf("org1", "api2")} returns listOf(Api())
        coEvery { publisher invoke "fetchPublishedApiVersions" withArguments listOf("org2", "api1")} returns listOf(Api(), Api())
        coEvery { publisher invoke "fetchPublishedApiVersions" withArguments listOf("org2", "api3")} returns listOf(Api(), Api(), Api())

        val apis = runBlocking { publisher.fetchPublished()}
        "should fetch all available APIs in distinct units" {
            apis.size shouldBe 7
        }
    }
})