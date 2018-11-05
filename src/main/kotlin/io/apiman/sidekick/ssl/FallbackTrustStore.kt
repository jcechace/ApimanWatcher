package io.apiman.sidekick.ssl

import java.security.KeyStore
import java.security.cert.X509Certificate

import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import java.io.FileInputStream
import java.security.cert.CertificateException


/**
 * Wrapper Truststore implementation which falls back to default Java trust store if Certificate is not found in
 * provided jks
 *
 * @author Jakub Cechacek
 */
class FallbackTruststoreManager(path: String?, password: String?) : X509TrustManager {

    /**
     * Default Java trust store
     */
    private val defaultTm : X509TrustManager  by lazy {
        val defaultTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        defaultTmf.init(null as KeyStore?)
        defaultTmf.trustManagers.find { it is X509TrustManager } as X509TrustManager
    }

    /**
     * Trust store loaded from provided *.jks file
     */
    private val customTm: X509TrustManager? by lazy {
        path?.run {
            val customTs = KeyStore.getInstance(KeyStore.getDefaultType())
            FileInputStream(path).use { customTs.load(it, password?.toCharArray()) }
            val customTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            customTmf.init(customTs)
            customTmf.trustManagers.find { it is X509TrustManager } as X509TrustManager
        }
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        try {
            customTm?.checkClientTrusted(chain, authType) ?: throw CertificateException()
        } catch (e : CertificateException) {
            defaultTm.checkClientTrusted(chain, authType)
        }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        try {
            customTm?.checkServerTrusted(chain, authType) ?: throw CertificateException()
        } catch (e : CertificateException) {
            defaultTm.checkServerTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        val acceptedIssuer = customTm?.acceptedIssuers ?: emptyArray()
        return acceptedIssuer + defaultTm.acceptedIssuers
    }
}

