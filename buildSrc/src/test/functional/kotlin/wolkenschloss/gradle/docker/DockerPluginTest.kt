package wolkenschloss.gradle.docker

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Template
import wolkenschloss.testing.build
import java.io.File

class DockerPluginTest : DescribeSpec({

    describe("DockerPlugin applied to a gradle project with ImageBuildTask") {
        autoClose(Template("image")).withClone {
            it("should build image") {
                val result = build("base")

                result.task(":base")!!.outcome shouldBe TaskOutcome.SUCCESS

                val imageIdFile = workingDirectory.resolve("build/.docker/fixture-image/base")
                imageIdFile.shouldExist()
            }

            it("should execute cat task") {
                val result = build("cat")

                result.task(":cat")!!.outcome shouldBe TaskOutcome.SUCCESS
                result.output shouldContain "Hello Cat"
            }

            it("should capture output with log level info") {
                val result = build("echo", "-i")

                result.task(":echo")?.outcome shouldBe TaskOutcome.SUCCESS
                result.output shouldContain "Hello Echo"
            }

            it("should not capture output with log level lifecycle") {
                val result = build("silent")
                result.task(":silent")?.outcome shouldBe TaskOutcome.SUCCESS
                result.output shouldNotContain "Hello Silence"
            }

            it("should not capture output with log level info") {
                val result = build("silent", "-i", "--rerun-tasks")
                result.task(":silent")?.outcome shouldBe TaskOutcome.SUCCESS
                result.output shouldContain "Hello Silence"
            }

            // see https://unix.stackexchange.com/questions/18743/whats-the-point-in-adding-a-new-line-to-the-end-of-a-file
            it("should append new line to container output logfile") {
                val result = build("log", "--rerun-tasks", "-i")

                result.task(":log")?.outcome shouldBe TaskOutcome.SUCCESS
                workingDirectory.resolve("build/log").readText() shouldBe "Hello Logfile\n"
            }
        }
    }


    describe("DockerPlugin applied to a gradle project mounting volumes") {
        autoClose(Template("mount")).withClone {
            it("should read from mounted file") {

                val dataFile = workingDirectory.resolve("volumes/datafile")
                dataFile.parentFile.mkdirs()
                dataFile.writeText("content of mounted file")

                assertSoftly(build("catFile")) {
                    task(":catFile")!!.outcome shouldBe TaskOutcome.SUCCESS
                    output shouldContain "content of mounted file"
                }
            }

            it("should read from mounted directory") {
                val relativeDataDirectory = File("volumes/data")
                val dataFile = workingDirectory.resolve(relativeDataDirectory).resolve("datafile")
                dataFile.parentFile.mkdirs()
                dataFile.writeText("another content of mounted volume")

                assertSoftly(build("catDir", "-Pvolume=${relativeDataDirectory.path}")) {
                    task(":catDir")!!.outcome shouldBe TaskOutcome.SUCCESS
                    output shouldContain "another content of mounted volume"
                }
            }

            it("should write a file into mounted directory") {
                val dataDir = workingDirectory.resolve("build/volumes/data").absoluteFile
                dataDir.mkdirs()

                val result = build("write", "-P", "text=hello write")
                result.task(":write")!!.outcome shouldBe TaskOutcome.SUCCESS
                dataDir.resolve("result").readText() shouldBe "hello write"
            }
        }
    }
})

