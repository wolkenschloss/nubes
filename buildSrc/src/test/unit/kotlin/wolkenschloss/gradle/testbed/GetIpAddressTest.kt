package wolkenschloss.gradle.testbed


import io.kotest.core.spec.style.FunSpec

import io.kotest.matchers.shouldNotBe

class GetIpAddressTest : FunSpec({

    test("host address is not localhost") {
        IpUtil.hostAddress shouldNotBe "127.0.0.1"
    }
})