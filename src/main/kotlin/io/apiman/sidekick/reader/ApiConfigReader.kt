package io.apiman.sidekick.reader

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClient

/**
 * Implementations of this interface are intended to extract Gateway specific API configuration from k8 cluster
 *
 * @param T Gateway specific API configuration type
 * @author Jakub Cechacek
 */
interface ApiConfigReader<T> {

    val k8Client: KubernetesClient

    /**
     * Read API specification from k8 cluster
     *
     * @return API object for API Management gateway
     */
    fun read(resource: Service, shallow: Boolean = false): T
}