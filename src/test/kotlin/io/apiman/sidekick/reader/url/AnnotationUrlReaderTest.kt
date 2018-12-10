package io.apiman.sidekick.reader.url

import io.apiman.sidekick.DiscoveryAnnotationConfig
import io.fabric8.kubernetes.api.model.Service
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor

class AnnotationUrlReaderTest : BehaviorSpec({
    given("URL reader with basic service") {
        mockkConstructor(DiscoveryAnnotationConfig::class)
        every { anyConstructed<DiscoveryAnnotationConfig>().port } returns "port"
        every { anyConstructed<DiscoveryAnnotationConfig>().path } returns "path"
        every { anyConstructed<DiscoveryAnnotationConfig>().scheme } returns "scheme"

        val resource = mockk<Service>(relaxed = true)
        val reader = AnnotationUrlReader(resource)

        every { resource.metadata.name } returns "my-service"
        every { resource.metadata.namespace } returns "generic-project"
        every { resource.metadata.annotations.get("port") } returns null
        every { resource.metadata.annotations.get("path") } returns null
        every { resource.metadata.annotations.get("scheme") } returns null

        `when`("Only basic annotations are present") {
            then("Url should be correct") {
                reader.readUrl().toString() shouldBe "http://my-service.generic-project.svc/"
            }
        }

        `when`("Port annotation is present") {
            every { resource.metadata.annotations.get("port") } returns "8080"
            then("Url should be correct") {
                reader.readUrl().toString() shouldBe "http://my-service.generic-project.svc:8080/"
            }
        }

        `when`("Path and port annotations are present") {
            every { resource.metadata.annotations.get("port") } returns "8081"
            every { resource.metadata.annotations.get("path") } returns "home/test"
            then("Url should be correct") {
                reader.readUrl().toString() shouldBe "http://my-service.generic-project.svc:8081/home/test"
            }
        }

        `when`("Scheme, Path and port annotations are present") {
            every { resource.metadata.annotations.get("port") } returns "8082"
            every { resource.metadata.annotations.get("path") } returns "home/test/test"
            every { resource.metadata.annotations.get("scheme") } returns "https"
            then("Url should be correct") {
                reader.readUrl().toString() shouldBe "https://my-service.generic-project.svc:8082/home/test/test"
            }
        }
    }
})