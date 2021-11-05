package familie.haschka.wolkenschloss.cookbook.testing;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class IpUtil {

    public static String getHostAddress() throws UnknownHostAddress {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("1.1.1.1"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new UnknownHostAddress("Can not determine IP Address of host", e);
        }
    }
}
