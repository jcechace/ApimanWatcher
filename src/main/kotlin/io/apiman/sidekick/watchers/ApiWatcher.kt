package io.apiman.sidekick.watchers

import io.apiman.sidekick.appConfig
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.Watch
import io.fabric8.kubernetes.client.Watcher
import kotlinx.coroutines.coroutineScope

/**
 * Extension to fabric8 k8 watcher used to watch k8 in more simplified way
 *
 * @author Jakub Cechacek
 */
interface ApiWatcher : Watcher<Service> {
    val k8Client: KubernetesClient

    /**
     * Initial operations performed before the watch is started
     */
    suspend fun onStartup()

    /**
     * Default watcher
     */
    suspend fun watch(): Watch = k8Client
        .services()
        .inAnyNamespace()
        .withLabel(appConfig().discovery.label)
        .watch(this)

    /**
     * Default launch order
     */
    suspend fun launch() = coroutineScope {
        onStartup()
        watch()
    }
}