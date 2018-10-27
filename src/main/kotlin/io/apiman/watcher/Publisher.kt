package io.apiman.watcher

import io.apiman.gateway.engine.beans.Api

/**
 *
 * @author Jakub Cechacek
 */
interface Publisher<T> {

    suspend fun publishApi(api: T) : Boolean

    suspend fun retireApi(api: T) : Boolean
}