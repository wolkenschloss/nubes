package wolkenschloss.gradle.testbed

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.gradle.testbed.domain.DomainExtension
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build
import wolkenschloss.testing.buildAndFail
import wolkenschloss.testing.createRunner

class TestbedPluginSpec : FunSpec({
    context("A Project with testbed plugin applied to") {
        test("transform should copy files") {
            Fixtures("testbed/transform/copy").withClone {
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
            Fixtures("testbed/transform/replace").withClone {
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

        test("should fail if the domain-suffix system property is missing") {
            Fixtures("testbed/suffix/missing").withClone {
                assertSoftly(buildAndFail("help")) {
                    output shouldContain DomainExtension.ERROR_DOMAIN_SUFFIX_NOT_SET
                }
            }
        }

        test("should build successfully if the domain-suffix system property is passed as a parameter") {
            Fixtures("testbed/suffix/missing").withClone {
                val result = createRunner()
                    .withArguments("help", "-D${DomainExtension.DOMAIN_SUFFIX_PROPERTY}=\"host.local\"")
                    .build()
                result.task(":help")!!.outcome shouldBe TaskOutcome.SUCCESS
            }
        }
    }
})
