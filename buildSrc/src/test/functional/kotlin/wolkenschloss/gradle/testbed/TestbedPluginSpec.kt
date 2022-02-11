package wolkenschloss.gradle.testbed

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build

class TestbedPluginSpec : FunSpec({
    context("A Project with testbed plugin applied to") {
        test("transform should copy files") {
            val fixture = Fixtures("testbed/transform/copy")
            fixture.withClone {
                val result = build("transform", "-i")

                result.task(":transform")!!.outcome shouldBe TaskOutcome.SUCCESS

                assertSoftly(resolve("build/config")) {
                    with(resolve("cloud-init")) {
                        shouldContainFile("user-data")
                        shouldContainFile("network-config")
                    }
                    with(resolve("vm")) {
                        shouldContainFile("domain.xml")
                        shouldContainFile("pool.xml")
                    }
                }
            }
        }

        test("transform should replace content") {
            val fixture = Fixtures("testbed/transform/replace")
            fixture.withClone {
                val result = build("transform", "-i")

                result.task(":transform")!!.outcome shouldBe TaskOutcome.SUCCESS

                resolve("build/config/example").readText() shouldBe """
                    testbed
                    testbed.wolkenschloss.local
                    ${System.getProperty("user.name")}
                    ${System.getenv("LANG")}
                    ${IpUtil.hostAddress}
                    9191
                    ${resolve("build/pool").absolutePath}
                    root.qcow2
                    cidata.img
                """.trimIndent()
            }
        }
    }
})
