package io.apiman.sidekick.reader

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClient

/**
 *
 * @author Jakub Cechacek
 */
interface ApiConfigReader<T> {

    val k8Client: KubernetesClient

    fun read(resource: Service, shallow: Boolean = false): T
}