package io.apiman.watcher

import io.apiman.gateway.engine.beans.Api
import io.apiman.watcher.publishers.Publisher
import io.apiman.watcher.reader.ConfigReader
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.Watcher
import kotlinx.coroutines.runBlocking
import mu.KLogging

/**
 *
 * @author Jakub Cechacek
 */
class ApiWatcher(
    val reader: ConfigReader<Api>,
    val publisher: Publisher<Api>
) : Watcher<Service> {

    companion object : KLogging()

    private fun resourceAdded(resource: Service) {
        val api = reader.read(resource)
        runBlocking {
            publisher.publishApi(api)
        }
        logger.info { "[ADDED]  ${resource.metadata.namespace}:${resource.metadata.name}" }
    }

    private fun resourceDeleted(resource: Service) {
        val api = reader.read(resource, shallow = true)
        runBlocking {
            publisher.retireApi(api)
        }
        logger.info("[DELETED] ${resource.metadata.namespace}:${resource.metadata.name}")
    }

    override fun eventReceived(action: Watcher.Action, resource: Service) {
        logger.debug { "Watcher received ${action} action" }
        when (action) {
            Watcher.Action.ADDED -> resourceAdded(resource)
            Watcher.Action.DELETED -> resourceDeleted(resource)
            else -> {
            }
        }
    }

    override fun onClose(cause: KubernetesClientException?) {
        if (cause != null) {
            logger.error { cause }
        }
    }
}