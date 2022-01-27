package wolkenschloss.gradle.docker

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class DockerServiceTest : DescribeSpec({

    val fixtures = File("src/test/fixtures").absoluteFile

    describe("A project using DockerPlugin") {
        val projectDir = tempdir()

        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()

        afterSpec {
            DockerService.getInstance(project.gradle).close()
        }

        project.pluginManager.apply(DockerPlugin::class.java)

        describe("with task of type BuildImageTask") {
            project.tasks {

                val hello by registering(BuildImageTask::class)

                it("should have have docker service instance") {
                    hello.get().dockerService.get()
                        .shouldBe(DockerService.getInstance(project.gradle))
                }

                describe("configured with base directory") {
                    afterTest {
                        hello.forceRemoveImage()
                    }

                    hello {
                        inputDir.set(fixtures.resolve("docker/hello"))
                    }

                    it("should build docker image") {
                        hello.get().execute()

                        val imageId = hello.get().imageId.get().asFile.readText()
                        val docker = hello.get().dockerService.get().client
                        val result = docker.listImagesCmd()
                            .withShowAll(true)
                            .exec()

                        result.map { it.id }
                            .map { it.removePrefix("sha256:") }
                            .map { it.take(imageId.length) }
                            .shouldContain(imageId)
                    }
                }
            }
        }
    }
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf
}

private fun TaskProvider<BuildImageTask>.forceRemoveImage() {
    val task = get()
    val imageId = task.imageId.get().asFile.readText()
    task.dockerService.get()
        .client.removeImageCmd(imageId)
        .withForce(true)
        .exec()
}