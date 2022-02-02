package wolkenschloss.gradle.docker

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build

class DockerPluginTest : DescribeSpec({

    describe("DockerPlugin applied to a gradle project with ImageBuildTask") {
        val fixture = Fixtures("image").clone(tempdir())

        it("should build image") {
            val result = fixture.build("base")

            result.task(":base")?.outcome shouldBe TaskOutcome.SUCCESS

            val imageIdFile = fixture.resolve("build/.docker/example/base")
            imageIdFile.shouldExist()
        }

        it("should execute cat task") {
            val result = fixture.build("cat")

            result.task(":cat")!!.outcome shouldBe TaskOutcome.SUCCESS
            result.output shouldContain "Hello Cat"
        }

        it("should capture output with log level info") {
            val result = fixture.build("echo",  "-i")

            result.task(":echo")?.outcome shouldBe TaskOutcome.SUCCESS
            result.output shouldContain "Hello Echo"
        }

        it("should not capture output with log level lifecycle") {
            val result = fixture.build("silent")
            result.task(":silent")?.outcome shouldBe TaskOutcome.SUCCESS
            result.output shouldNotContain "Hello Silence"
        }

        it("should not capture output with log level info") {
            val result = fixture.build("silent", "-i", "--rerun-tasks")
            result.task(":silent")?.outcome shouldBe TaskOutcome.SUCCESS
            result.output shouldContain "Hello Silence"
        }


        // see https://unix.stackexchange.com/questions/18743/whats-the-point-in-adding-a-new-line-to-the-end-of-a-file
        it("should append new line to container output logfile") {
            val result = fixture.build("log", "--rerun-tasks", "-i")

            println(result.output)
            result.task(":log")?.outcome shouldBe TaskOutcome.SUCCESS
            fixture.resolve("build/log").readText() shouldBe "Hello Logfile\n"
        }
    }
})

