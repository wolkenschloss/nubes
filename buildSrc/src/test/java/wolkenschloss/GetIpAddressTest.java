package wolkenschloss;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GetIpAddressTest {

    Logger logger = LoggerFactory.getLogger(GetIpAddressTest.class);

    @Test
    public void getIp() {
        Assertions.assertNotEquals(IpUtil.getHostAddress(), "127.0.0.1");
    }

    @Test
    public void getIpViaGetLocalHost() throws UnknownHostException {

        var adress = InetAddress.getLocalHost();
        logger.info(() -> String.format("Hostname: %s%n", adress.getHostName()));
        logger.info(() -> String.format("IP: %s%n", adress.getHostAddress()));

        var other = InetAddress.getByName(adress.getHostName());
        logger.info(() -> String.format("Hostname: %s%n", other.getHostName()));
        logger.info(() -> String.format("Hostname: %s%n", other.getCanonicalHostName()));
        logger.info(() -> String.format("IP: %s%n", other.getHostAddress()));

    }

//    @Test
//    public void getClientIp() throws LibvirtException {
//       var connection = new  Connect("qemu:///system");
//       var network = connection.networkLookupByName("default");
//       var domain = connection.domainLookupByName("testbed");
//    }
}
