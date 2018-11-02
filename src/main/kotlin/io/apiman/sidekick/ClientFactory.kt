package io.apiman.sidekick

import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.client.HttpClient
import io.ktor.client.features.auth.basic.BasicAuth
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature

/**
 *
 * @author Jakub Cechacek
 */
object ClientFactory {

    fun k8Client() : KubernetesClient {
        val config = ConfigBuilder()
            .withMasterUrl(appConfig().openshift.url)
            .withTrustCerts(true)
            .withWebsocketPingInterval(30_000L)
            .withWebsocketTimeout(30_0000L)
            .build()

        return DefaultKubernetesClient(config)
    }

    fun apimanHttpClient() : HttpClient {
        return HttpClient {
            install(JsonFeature) {
                serializer = GsonSerializer {
                    serializeNulls()
                    disableHtmlEscaping()
                }
            }
            install(BasicAuth) {
                username = "apiman"
                password = "password"
            }
        }
    }
}