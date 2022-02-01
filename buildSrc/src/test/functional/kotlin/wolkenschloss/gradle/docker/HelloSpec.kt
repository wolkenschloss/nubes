package wolkenschloss.gradle.docker

import io.kotest.core.script.describe
import io.kotest.core.spec.style.FunSpec

class HelloSpec : FunSpec({

    describe("hello") {
        test("should say") {
            println("hello")
        }
    }
}) {
}