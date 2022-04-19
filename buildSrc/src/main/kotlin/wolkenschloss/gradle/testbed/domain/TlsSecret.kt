package wolkenschloss.gradle.testbed.domain
import wolkenschloss.gradle.ca.CertificateWrapper
import net.minidev.json.JSONObject
import java.util.*

class TlsSecret(val namespace: String, val name: String, val certificate: CertificateWrapper) {

    override fun toString(): String {
        return "$namespace/$name"
    }

    companion object {
        fun parse(json: JSONObject): TlsSecret {
            val data = json.get("data") as JSONObject
            val certificate = data.getAsString("tls.crt").decode()
            val metadata = json.get("metadata") as JSONObject
            val name = metadata.getAsString("name")
            val namespace = metadata.getAsString("namespace")

            return TlsSecret(namespace, name, CertificateWrapper.parse(certificate))
        }

        private fun String.decode(): String {
            return String(Base64.getDecoder().decode(this))
        }
    }
}
