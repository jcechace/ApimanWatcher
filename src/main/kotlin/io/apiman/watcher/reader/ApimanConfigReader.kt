package io.apiman.watcher.reader

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.apiman.gateway.engine.beans.Api
import io.apiman.gateway.engine.beans.Policy
import io.apiman.watcher.WatcherConfiguration
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClient
import mu.KLogging

class ApimanConfigReader(override val k8Client: KubernetesClient) : ConfigReader<Api> {
    companion object : KLogging()

    override fun read(resource: Service, shallow: Boolean): Api {
        val api = Api().apply {
            apiId = resource.metadata.name
            organizationId = resource.metadata.namespace;
            endpointType = "rest"
            endpointContentType = "json"
            isPublicAPI = true
            version = "1.0"
        }

        if (!shallow) {
            readPolicies(api, resource)
        }
        return api
    }

    private fun readPolicies(api: Api, resource: Service) {
        logger.info { "Trying to read policy configuration for api '${api.organizationId}:${api.apiId}'" }
        val annotations = resource.metadata.annotations
        val cmName = annotations.getOrDefault(
            key = WatcherConfiguration.DISCOVERY_POLICIES,
            defaultValue = "${api.apiId}-policy-config"
        )

        logger.info { "Looking for config-map '${cmName}'" }
        val configMap = k8Client
            .configMaps()
            .inNamespace(resource.metadata.namespace)
            .withName(cmName)
            .get()

        api.apiPolicies = configMap?.run {
            val policies = mutableListOf<Policy>()
            data.forEach { _, value ->
                logger.debug { "Found policy configuration" }
                logger.debug { value }
                val type = object : TypeToken<Collection<Policy>>() {}.type
                policies.addAll(Gson().fromJson(value, type))
            }
            policies
        }
    }
}