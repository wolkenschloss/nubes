package wolkenschloss.gradle.docker

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import org.gradle.api.logging.LogLevel
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.kotlin.dsl.*

class RunContainerTaskTest : DescribeSpec ({
    describe("A project with DockerPlugin applied") {
        val log = CaptureStandardOutput()
        val projectDir = tempdir()
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()

        afterSpec {
            DockerService.getInstance(project.gradle).close()
        }

        project.pluginManager.apply(DockerPlugin::class.java)
        project.gradle.useLogger(log)

        describe("with task of type RunTask") {

            project.tasks {
                val hello by registering(RunContainerTask::class)


                it("should fail when no image is configured") {
                    hello.get().execute()
                }

                xit("should execute shell command") {
                    hello {
                        command.addAll("echo", "hello world")
                        logging.captureStandardOutput(LogLevel.QUIET)
                    }

                    hello.get().execute()
                    log.output shouldBe  "hello world"
                }
            }
        }
    }
}){
}

