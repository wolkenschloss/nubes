package wolkenschloss

import spock.lang.Specification

class GetIpAddressSpec extends Specification {

    def "host address is not localhost"() {
        when:
        def ipAddress = IpUtil.hostAddress

        then:
        ipAddress != "127.0.0.1"
    }
}
