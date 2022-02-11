package wolkenschloss.gradle.testbed

import org.gradle.api.GradleScriptException
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.net.UnknownHostException

object IpUtil {
    val hostAddress: String
        get() {
            try {
                DatagramSocket().use { socket ->
                    socket.connect(InetAddress.getByName("1.1.1.1"), 10002)
                    return socket.localAddress.hostAddress
                }
            } catch (e: SocketException) {
                throw GradleScriptException("I cannot find my IP address", e)
            } catch (e: UnknownHostException) {
                throw GradleScriptException("I cannot find my IP address", e)
            }
        }
}