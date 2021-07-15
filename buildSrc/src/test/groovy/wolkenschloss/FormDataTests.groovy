package wolkenschloss

import spock.lang.Specification
import wolkenschloss.domain.FormData

class FormDataTests extends Specification {

    FormData messageBody;


    def setup() {
        messageBody = new FormData("pub_key_ecdsa=ecdsa-sha2-nistp256+AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBFeY5xKUNG5uZ0mDXiSuFrUSEqxGjnhQJe1mlFMgUmuYwxbocZE23NUiMglMtYQ64Cn%2BTdOoHqko0baa95FkEVY%3D+root%40testbed%0A&instance_id=iid-local01&hostname=testbed&fqdn=testbed.wolkenschloss.local");
    }

    def "should get value"() {
        when:
        def value = messageBody.value("fqdn")

        then:
        value == "testbed.wolkenschloss.local"
    }


    def "should throw exception for unknown keys"() {
        when:
        messageBody.value("unknown")

        then:
        NoSuchElementException e = thrown()
        e != null
    }

    def "should decode value"() {
        when:
        def value = messageBody.value("pub_key_ecdsa")

        then:
        value == "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBFeY5xKUNG5uZ0mDXiSuFrUSEqxGjnhQJe1mlFMgUmuYwxbocZE23NUiMglMtYQ64Cn+TdOoHqko0baa95FkEVY= root@testbed"
    }
}
