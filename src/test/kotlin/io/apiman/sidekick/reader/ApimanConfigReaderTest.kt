package io.apiman.sidekick.reader

import io.apiman.sidekick.reader.url.AnnotationUrlReader
import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClient
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.ktor.http.Url
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot

class ApimanConfigReaderTest : FreeSpec({
    "ApimanConfigReader" - {
        val k8 = mockk<KubernetesClient>()
        val reader = ApimanConfigReader(k8)

        val resource = mockk<Service>(relaxed = true)

        every { resource.metadata.name } returns "my-service"
        every { resource.metadata.namespace } returns "generic-project"

        "should construct Correct API from basic service"  {
            val api = reader.read(resource, true)
            api.organizationId shouldBe "generic-project"
            api.apiId shouldBe "my-service"
        }

        "given service with policies" - {
            // Mock endpoint
            mockkConstructor(AnnotationUrlReader::class)
            every { anyConstructed<AnnotationUrlReader>().readUrl() } returns Url("http://my-service.generic-project.svc/")

            val configMap = mockk<ConfigMap>()
            val json = "[{'policyJsonConfig':'json', 'policyImpl':'my.package.MyPolicy'}]"
            every { configMap.data } returns mapOf(" " to json)
            val def = slot<String>()


            "in default config map" - {
                every { resource.metadata.annotations.getOrDefault(key = any(), defaultValue = capture(def))} answers { def.captured }
                every { k8
                    .configMaps()
                    .inNamespace(any())
                    .withName("my-service-policy-config")
                    .get() } returns configMap

                val api = reader.read(resource, false)
                "should construct correct API" {
                    api.organizationId shouldBe "generic-project"
                    api.apiId shouldBe "my-service"
                    api.endpoint shouldBe "http://my-service.generic-project.svc/"
                    api.apiPolicies[0].policyImpl shouldBe "my.package.MyPolicy"
                    api.apiPolicies[0].policyJsonConfig shouldBe "json"
                }
            }

            "in non default config map" - {
                every { k8
                    .configMaps()
                    .inNamespace(any())
                    .withName("my-own_config-map")
                    .get() } returns configMap
                every { resource.metadata.annotations.getOrDefault(key = "discovery.apiman.net/policies", defaultValue = any())} returns "my-own_config-map"

                val api = reader.read(resource, false)
                "should construct correct API" {
                    api.organizationId shouldBe "generic-project"
                    api.apiId shouldBe "my-service"
                    api.endpoint shouldBe "http://my-service.generic-project.svc/"
                    api.apiPolicies[0].policyImpl shouldBe "my.package.MyPolicy"
                    api.apiPolicies[0].policyJsonConfig shouldBe "json"
                }
            }
        }
    }
})

