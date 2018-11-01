package io.apiman.sidekick

import io.apiman.sidekick.publishers.ApimanPublisher
import io.apiman.sidekick.reader.ApimanConfigReader
import io.apiman.sidekick.watchers.ApimanWatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 *
 * @author Jakub Cechacek
 */
val k8Client = ClientFactory.k8Client()
val httpClient = ClientFactory.apimanHttpClient()

fun main(args: Array<String>) = runBlocking<Unit> {
    val publisher = ApimanPublisher(httpClient)
    val reader = ApimanConfigReader(k8Client = k8Client)
    val watcher = ApimanWatcher(k8Client, reader, publisher)

    launch {
        watcher.watch()
    }
}
