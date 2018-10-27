package io.apiman.watcher


import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.ktor.client.HttpClient
import io.ktor.client.features.auth.basic.BasicAuth
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature

/**
 *
 * @author Jakub Cechacek
 */

val OCP_TOKEN = "nxZjpPyzSy7kKVZKJilhXDnxAH7oGCCOImdlCdOwrzE"
val OCP_URL = "https://master.ocp.api-qe.eng.rdu2.redhat.com:443"


fun main(args: Array<String>) {
    val config = ConfigBuilder()
        .withMasterUrl(OCP_URL)
        .withTrustCerts(true)
        .withOauthToken(OCP_TOKEN)
        .withWebsocketPingInterval(30_000L)
        .withWebsocketTimeout(30_0000L)
        .build()

    val k8Client = DefaultKubernetesClient(config)

    val httpClient = HttpClient {
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
    val publisher = ApimanPublisher(httpClient)
    val watcher = ApiWatcher(k8Client, publisher)

    k8Client
        .services()
        .inAnyNamespace()
        .withLabel(WatcherConfiguration.ENABLING_LABEL_NAME)
        .watch(watcher)
}