package io.apiman.sidekick

import io.apiman.sidekick.ssl.FallbackTruststoreManager
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.auth.basic.BasicAuth
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import javax.net.ssl.SSLContext

/**
 *
 * @author Jakub Cechacek
 */

/**
 * K8 client factory
 */
fun k8Client() : KubernetesClient {
    val builder = ConfigBuilder()
        .withMasterUrl(appConfig().openshift.url)
        .withTrustCerts(true)
        .withWebsocketPingInterval(30_000L)
        .withWebsocketTimeout(30_0000L)

    appConfig().openshift.token?.let {
        builder.withOauthToken(it)
    }

    val config = builder.build()

    return DefaultKubernetesClient(config)
}

fun apimanHttpClient() : HttpClient {
    val config = appConfig()
    return HttpClient(OkHttp) {
        engine {
            config {
                val sslContext = SSLContext.getInstance("TLS")
                val trustManager = FallbackTruststoreManager(config.ssl?.path, config.ssl?.password)
                sslContext.init(null, arrayOf(trustManager), null);
                sslSocketFactory(sslContext.socketFactory, trustManager)
            }
        }
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                disableHtmlEscaping()
            }
        }
        install(BasicAuth) {
            username = config.apiman.username
            password = config.apiman.password
        }
    }
}