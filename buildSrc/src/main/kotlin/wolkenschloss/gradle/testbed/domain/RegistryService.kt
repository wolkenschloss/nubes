package wolkenschloss.gradle.testbed.domain

import com.google.cloud.tools.jib.api.*
import com.jayway.jsonpath.JsonPath
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.openssl.PEMParser
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.FileInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


class RegistryService(val name: String) {

    private fun loadCertificate(certificate: Provider<RegularFile>): SSLContext {
        certificate.get().asFile.reader().use {
            val parser = PEMParser(it)
            val certHolder = parser.readObject() as X509CertificateHolder
            val cert = JcaX509CertificateConverter().getCertificate(certHolder)

            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry("1", cert)

            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustManagerFactory.trustManagers, null)

            return sslContext
        }
    }

    fun listCatalogs(certificate: Provider<RegularFile>): List<String> {
        val client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .sslContext(loadCertificate(certificate)).build()

        val uri = URI.create("https://$name/v2/_catalog")
        val request = HttpRequest.newBuilder().uri(uri).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw GradleException(String.format("Return Code from Registry: %d", response.statusCode()))
        }
        val parse = JsonPath.parse(response.body())
        return parse.read("$.repositories[*]")
    }

    fun uploadImage(image: String, truststore: Provider<RegularFile>): String {
        configureTrustStore(truststore)
            val tag = String.format("%s/%s", name, image)
            Jib.from("hello-world")
                .containerize(
                    Containerizer.to(RegistryImage.named(tag))

//                    .setAllowInsecureRegistries(true)
                )
            return tag

    }

    private fun configureTrustStore(truststore: Provider<RegularFile>) {
        val jreTrustManager: X509TrustManager? = getJreTrustManager()
        val myTrustManager: X509TrustManager? = getMyTrustManager(truststore)
        val mergedTrustManager: X509TrustManager = createMergedTrustManager(jreTrustManager, myTrustManager)
        setSystemTrustManager(mergedTrustManager)
    }

    private fun getJreTrustManager(): X509TrustManager? {
        return findDefaultTrustManager(null)
    }

    private fun getMyTrustManager(truststore: Provider<RegularFile>): X509TrustManager? {
        // Adapt to load your keystore
        FileInputStream(truststore.get().asFile).use { myKeys ->
            val myTrustStore = KeyStore.getInstance("jks")
//            myTrustStore.load(myKeys, "password".toCharArray())
            myTrustStore.load(myKeys, null)
            return findDefaultTrustManager(myTrustStore)
        }
    }

    private fun findDefaultTrustManager(keyStore: KeyStore?): X509TrustManager? {
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore) // If keyStore is null, tmf will be initialized with the default trust store
        for (tm in tmf.trustManagers) {
            if (tm is X509TrustManager) {
                return tm as X509TrustManager
            }
        }
        return null
    }
    private fun setSystemTrustManager(mergedTrustManager: X509TrustManager) {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(mergedTrustManager), null)

        // You don't have to set this as the default context,
        // it depends on the library you're using.
        SSLContext.setDefault(sslContext)
    }

    private fun createMergedTrustManager(jreTrustManager: X509TrustManager?, customTrustManager: X509TrustManager?): X509TrustManager {
        return object : X509TrustManager {
            // If you're planning to use client-cert auth,
            // merge results from "defaultTm" and "myTm".


            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return jreTrustManager!!.acceptedIssuers
            }

            override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
                try {
                    customTrustManager!!.checkServerTrusted(chain, authType)
                } catch (e: CertificateException) {
                    // This will throw another CertificateException if this fails too.
                    jreTrustManager!!.checkServerTrusted(chain, authType)
                }
            }

            override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
                // If you're planning to use client-cert auth,
                // do the same as checking the server.
                jreTrustManager!!.checkClientTrusted(chain, authType)
            }
        }
    }
    override fun toString(): String {
        return "Registry{name='$name'}"
    }
}