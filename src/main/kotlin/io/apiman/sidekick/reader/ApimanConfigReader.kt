package io.apiman.sidekick.reader

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.apiman.gateway.engine.beans.Api
import io.apiman.gateway.engine.beans.Policy
import io.apiman.sidekick.appConfig
import io.apiman.sidekick.reader.url.AnnotationUrlReader
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClient
import mu.KLogging

class ApimanConfigReader(override val k8Client: KubernetesClient) : ApiConfigReader<Api> {
    companion object : KLogging()

    override fun read(resource: Service, shallow: Boolean): Api {
        logger.info {
            "Trying to read Api configuration for service '${resource.metadata.namespace}:${resource.metadata.name}'"
        }
        val api = Api().apply {
            organizationId = resource.metadata.namespace;
            apiId = resource.metadata.name
            version = "1.0"
        }

        if (shallow) {
            return api
        }

        return api.apply {
            api.apiPolicies = readPolicies(resource, "${apiId}-policy-config")
            endpoint = AnnotationUrlReader(resource).readUrl().toString()
            endpointType = "rest"
            endpointContentType = "json"
            isPublicAPI = true
        }
    }

    private fun readPolicies(resource: Service, defaultFrom: String): List<Policy> {
        logger.info { "Trying to read policy configuration" }
        val annotations = resource.metadata.annotations
        val cmName = annotations.getOrDefault(appConfig().discovery.apiman.annotations.policies, defaultFrom)

        logger.info { "Looking for config-map '${cmName}'" }
        val configMap = k8Client
            .configMaps()
            .inNamespace(resource.metadata.namespace)
            .withName(cmName)
            .get()

        val policies = mutableListOf<Policy>()

        configMap?.apply {
            data.forEach { _, value ->
                logger.debug { "Found policy configuration" }
                logger.debug { value }
                val type = object : TypeToken<Collection<Policy>>() {}.type
                policies.addAll(Gson().fromJson(value, type))
            }
        }

        return policies
    }
}