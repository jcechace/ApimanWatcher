package io.apiman.watcher

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.apiman.gateway.engine.beans.Api
import io.apiman.gateway.engine.beans.Policy
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.Watcher
import kotlinx.coroutines.runBlocking


/**
 *
 * @author Jakub Cechacek
 */
class ApiWatcher(
    val client: KubernetesClient,
    val publisher : Publisher<Api>
) : Watcher<Service> {

    fun readApi(resource: Service): Api {
        val api = Api().apply {
            apiId = resource.metadata.name
            organizationId = resource.metadata.namespace;
            endpointType = "rest"
            endpointContentType = "json"
            isPublicAPI = true
            version = "1.0"
        }


        return api
    }

    fun resourceAdded(resource: Service) {
        println("[ADDED]  ${resource.metadata.namespace}:${resource.metadata.name}")

        val api = Api().apply {
            apiId = resource.metadata.name
            organizationId = resource.metadata.namespace;
            endpointType = "rest"
            endpointContentType = "json"
            isPublicAPI = true
            version = "1.0"
        }

        val annotations = resource.metadata.annotations

        val cmName = annotations.getOrDefault(WatcherConfiguration.DISCOVERY_POLICIES,
            defaultValue = "${api.apiId}-policy-config"
        )

        val configMap = client
            .configMaps()
            .inNamespace(resource.metadata.namespace)
            .withName(cmName)
            .get()

        configMap?.data?.forEach { _, value ->
            val type = object : TypeToken<Collection<Policy>>() {}.type
            api.apiPolicies = Gson().fromJson<List<Policy>>(value, type)
        }

        runBlocking {
            publisher.publishApi(api)
        }
    }

    fun resourceDeleted(resource: Service) {
        println("[DELETED] ${resource.metadata.namespace}:${resource.metadata.name}")
        val api = Api().apply {
            apiId = resource.metadata.name
            organizationId = resource.metadata.namespace;
            version = "1.0"
        }

        runBlocking {
            publisher.retireApi(api)
        }
    }

    override fun eventReceived(action: Watcher.Action, resource: Service) {
        when (action) {
            Watcher.Action.ADDED -> resourceAdded(resource)
            Watcher.Action.DELETED ->  resourceDeleted(resource)
            Watcher.Action.ERROR -> println(resource)
            else -> {}
        }
    }

    override fun onClose(cause: KubernetesClientException) {
        System.err.println(cause.message)
    }
}