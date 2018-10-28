package io.apiman.watcher.publishers

/**
 *
 * @author Jakub Cechacek
 */
interface Publisher<T> {

    suspend fun publishApi(api: T) : Boolean

    suspend fun retireApi(api: T) : Boolean
}