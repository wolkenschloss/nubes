package wolkenschloss.gradle.testbed.domain

import com.jayway.jsonpath.JsonPath
import org.gradle.api.provider.Provider
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream

class DomainOperations(private val execOperations: ExecOperations, private val domainName: Provider<String>)  {

    fun ipAddress(): String {
        ByteArrayOutputStream().use { stdout ->
            execOperations.exec {
                commandLine("multipass", "exec", domainName.get(), "--", "ip", "-p", "-json", "a")
                standardOutput = stdout;
            }

            val path = "\$[?(@.ifname=='enp5s0')].addr_info[?(@.family=='inet')].local"
            return JsonPath.parse(stdout.toString()).read<List<String>>(path).single()
        }
    }
}