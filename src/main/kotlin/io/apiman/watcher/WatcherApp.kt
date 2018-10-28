package io.apiman.watcher

import io.apiman.watcher.publishers.ApimanPublisher
import io.apiman.watcher.reader.ApimanConfigReader

/**
 *
 * @author Jakub Cechacek
 */

fun main(args: Array<String>) {
    val k8Client = ClientFactory.k8Client()
    val httpClient = ClientFactory.apimanHttpClient()

    val publisher = ApimanPublisher(httpClient)
    val reader = ApimanConfigReader(k8Client = k8Client)
    val watcher = ApiWatcher(reader = reader, publisher = publisher)

    k8Client
        .services()
        .inAnyNamespace()
        .withLabel(WatcherConfiguration.ENABLING_LABEL_NAME)
        .watch(watcher)
}