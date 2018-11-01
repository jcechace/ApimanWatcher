package io.apiman.watcher.publishers

import io.apiman.gateway.engine.beans.Api

/**
 *
 * @author Jakub Cechacek
 */
interface ApiPublisher<T> {

    suspend fun fetchPublished() : List<Api>

    suspend fun publishApi(api: T) : Boolean

    suspend fun retireApi(api: T) : Boolean
}