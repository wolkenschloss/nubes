package wolkenschloss.gradle.docker

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
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
                        val docker = DockerService.getInstance(project.gradle)

                        docker.listImages()
                            .map {it.shortId}
                            .shouldContain(imageId)
                    }
                }
            }
        }
    }
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf
}



