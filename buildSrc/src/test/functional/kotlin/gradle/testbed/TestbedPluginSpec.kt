package family.haschka.wolkenschloss.gradle.testbed

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.TaskOutcome
import family.haschka.wolkenschloss.gradle.testbed.domain.DomainExtension
import family.haschka.wolkenschloss.testing.Template
import family.haschka.wolkenschloss.testing.build
import family.haschka.wolkenschloss.testing.buildAndFail
import family.haschka.wolkenschloss.testing.createRunner

class TestbedPluginSpec : FunSpec({
    context("A Project with testbed plugin applied to") {
        test("transform should copy files") {
            Template("testbed/transform/copy").withClone {
                val result = build("transform", "-i")

                result.task(":transform")!!.outcome shouldBe TaskOutcome.SUCCESS

                assertSoftly(workingDirectory.resolve("build/config")) {
                    with(resolve("cloud-init")) {
                        shouldContainFile("user-data")
                    }
                }
            }
        }

        test("transform should replace content") {
            Template("testbed/transform/replace").withClone {
                val result = build("transform", "-i")

                result.task(":transform")!!.outcome shouldBe TaskOutcome.SUCCESS

                workingDirectory.resolve("build/config/example").readText() shouldBe """
                    testbed
                    testbed.wolkenschloss.test
                    ${System.getenv("LANG")}
                    
                """.trimIndent()
            }
        }

        test("should fail if the domain-suffix system property is missing") {
            Template("testbed/suffix/missing").withClone {
                assertSoftly(buildAndFail("help")) {
                    output shouldContain DomainExtension.ERROR_DOMAIN_SUFFIX_NOT_SET
                }
            }
        }

        test("should build successfully if the domain-suffix system property is passed as a parameter") {
            Template("testbed/suffix/missing").withClone {
                val result = createRunner()
                    .withArguments("help", "-D${DomainExtension.DOMAIN_SUFFIX_PROPERTY}=\"host.test\"")
                    .build()
                result.task(":help")!!.outcome shouldBe TaskOutcome.SUCCESS
            }
        }
    }
})
