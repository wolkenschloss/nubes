package family.haschka.wolkenschloss.gradle.testbed.domain
import family.haschka.wolkenschloss.gradle.ca.CertificateWrapper
import net.minidev.json.JSONObject
import family.haschka.wolkenschloss.gradle.ca.InvalidArgumentException
import java.util.*

class TlsSecret(val namespace: String, val name: String, val certificate: CertificateWrapper, val type: String) {

    override fun toString(): String {
        return "$namespace/$name"
    }

    companion object {
        fun containsCertificate(json: JSONObject): Boolean {
            val type = json.getAsString("type")
            if (type == "kubernetes.io/tls") {
                return true
            }
            if (type == "Opaque") {
                if (json.containsKey("data")) {
                    val data = json["data"] as JSONObject
                    if (data.containsKey("crt.pem")) {
                        return true
                    }
                }
            }

            return false
        }

        fun parse(json: JSONObject): TlsSecret {

            val metadata = json["metadata"] as JSONObject
            val name = metadata.getAsString("name")
            val namespace = metadata.getAsString("namespace")

            val type = json.getAsString("type")
            if (type == "Opaque") {
                if (json.containsKey("data")) {
                    val data = json["data"] as JSONObject
                    if (data.containsKey("crt.pem")) {
                        val certificate = data.getAsString("crt.pem").decode()
                        return TlsSecret(namespace, name, CertificateWrapper.parse(certificate), type)
                    }
                }
            }

            if (type == "kubernetes.io/tls") {
                val data = json["data"] as JSONObject
                val certificate = data.getAsString("tls.crt").decode()
                return TlsSecret(namespace, name, CertificateWrapper.parse(certificate), type)
            }

            throw InvalidArgumentException("$namespace/$name")
        }

        private fun String.decode(): String {
            return String(Base64.getDecoder().decode(this))
        }
    }
}
