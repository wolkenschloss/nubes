package wolkenschloss.gradle.testbed

import com.jayway.jsonpath.JsonPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import wolkenschloss.testing.Template

class DomainOperationsSpec : FunSpec({
    context("eine hosts datei") {
        test("kann ich lesen") {
            Template("testbed/hosts").withClone {
                val hosts = this.workingDirectory.resolve("hosts").readText()
                @Suppress("SpellCheckingInspection")
                val path = "\$[?(@.ifname=='ens3')].addr_info[?(@.family=='inet')].local"
                val result = JsonPath.parse(hosts).read<List<String>>(path)
                result.single() shouldBe "10.45.98.149"
            }
        }
    }
})