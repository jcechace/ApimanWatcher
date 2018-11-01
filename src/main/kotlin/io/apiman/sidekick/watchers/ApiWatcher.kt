package io.apiman.sidekick.watchers

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.Watcher
import kotlinx.coroutines.coroutineScope

/**
 *
 * @author Jakub Cechacek
 */
interface ApiWatcher : Watcher<Service> {
    suspend fun onStartup()
    suspend fun watch()

    suspend fun launch() = coroutineScope {
        onStartup()
        watch()
    }
}