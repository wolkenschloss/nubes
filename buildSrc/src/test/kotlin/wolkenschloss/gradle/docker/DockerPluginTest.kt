package wolkenschloss.gradle.docker

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

class DockerPluginTest : DescribeSpec({

    val fixtures = File("fixtures").absoluteFile
    val fixture = tempdir()

    describe("DockerPlugin applied to a gradle project with ImageBuildTask") {
        fixtures.resolve("image")
            .copyRecursively(fixture)

        it("should build image") {
            val result = fixture.run("base")

            result.task(":base")?.outcome shouldBe TaskOutcome.SUCCESS

            val imageIdFile = fixture.resolve("build/.docker/example/base")
            imageIdFile.shouldExist()
        }

        it("should execute cat task") {
            val result = fixture.run("cat")

            result.task(":cat")!!.outcome shouldBe TaskOutcome.SUCCESS
            result.output shouldContain "Hello Cat"
            fixture.resolve("build/hosts").readText() shouldBe "Hello Cat"
        }

        it("should capture output with log level info") {
            val result = fixture.run("echo",  "-i")

            result.task(":echo")?.outcome shouldBe TaskOutcome.SUCCESS
            result.output shouldContain "Hello Echo"
        }

        it("should not capture output with log level lifecycle") {
            val result = fixture.run("silent")
            result.task(":silent")?.outcome shouldBe TaskOutcome.SUCCESS
            result.output shouldNotContain "Hello Silence"
        }

        it("should not capture output with log level info") {
            val result = fixture.run("silent", "-i")
            result.task(":silent")?.outcome shouldBe TaskOutcome.SUCCESS
            result.output shouldContain "Hello Silence"
        }

        xit("should write container output to logfile") {
            val result = fixture.run("log", "--rerun-tasks", "-i")

            println(result.output)
            result.task(":log")?.outcome shouldBe TaskOutcome.SUCCESS
            fixture.resolve("build/log").readText() shouldBe "Hello Logfile"
        }
    }
}) {
}

private fun File.run(vararg args: String) = GradleRunner.create()
    .withProjectDir(this)
    .withArguments(*args)
    .withPluginClasspath()
    .build()