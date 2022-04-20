package wolkenschloss.gradle.testbed.domain

import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import net.minidev.json.parser.JSONParser
import org.gradle.api.provider.Provider
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream

class DomainOperations(private val execOperations: ExecOperations, private val domainName: Provider<String>)  {

    fun ipAddress(): String {
        ByteArrayOutputStream().use { stdout ->
            execOperations.exec {
                commandLine("multipass", "exec", domainName.get(), "--", "ip", "-p", "-json", "a")
                standardOutput = stdout
            }

            val path = "\$[?(@.ifname=='enp5s0')].addr_info[?(@.family=='inet')].local"
            return JsonPath.parse(stdout.toString()).read<List<String>>(path).single()
        }
    }
    fun readAllTlsSecrets(): List<TlsSecret> {

        val multipass = domainName.map { listOf("multipass", "exec", it, "--") }

        ByteArrayOutputStream().use {
            execOperations.exec {
                standardOutput = it
                commandLine = multipass.map {
                    it + listOf(
                        "/bin/bash", "-c",
                        "microk8s kubectl get secrets --all-namespaces -o json"
                    )
                }.get()
            }

            val parser = JSONParser(JSONParser.MODE_JSON_SIMPLE)
            var json = parser.parse(it.toString()) as JSONObject
            var items = json.get("items") as JSONArray

            return items
                .map { it as JSONObject }
                .filter { TlsSecret.containsCertificate(it) }
                .map { TlsSecret.parse(it) }
        }
    }
}