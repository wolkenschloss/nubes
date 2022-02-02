package wolkenschloss.gradle.testbed
import io.kotest.core.spec.style.FunSpec
import wolkenschloss.domain.FormData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.should
import io.kotest.matchers.string.startWith
import io.kotest.assertions.throwables.shouldThrowExactly
import java.util.NoSuchElementException

class FormDataTest : FunSpec({
    val messageBody = FormData("pub_key_ecdsa=ecdsa-sha2-nistp256+AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBFeY5xKUNG5uZ0mDXiSuFrUSEqxGjnhQJe1mlFMgUmuYwxbocZE23NUiMglMtYQ64Cn%2BTdOoHqko0baa95FkEVY%3D+root%40testbed%0A&instance_id=iid-local01&hostname=testbed&fqdn=testbed.wolkenschloss.local")

    test("should get value") {
        messageBody.value("fqdn")  shouldBe "testbed.wolkenschloss.local"
    }
    test("should throw exception for unknown keys") {
        val exception = shouldThrowExactly<NoSuchElementException>() {
            messageBody.value("unknown")
        }

        exception.message should startWith("unknown")
    }
    test("should decode value") {
        messageBody.value("pub_key_ecdsa") shouldBe "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBFeY5xKUNG5uZ0mDXiSuFrUSEqxGjnhQJe1mlFMgUmuYwxbocZE23NUiMglMtYQ64Cn+TdOoHqko0baa95FkEVY= root@testbed"
    }
})