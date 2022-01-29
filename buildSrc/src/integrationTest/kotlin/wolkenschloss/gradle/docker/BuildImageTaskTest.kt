package wolkenschloss.gradle.docker

import com.github.dockerjava.api.exception.DockerClientException
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.testfixtures.ProjectBuilder
import wolkenschloss.testing.Fixtures

class BuildImageTaskTest : DescribeSpec({

    describe("A project with DockerPlugin applied") {
        val projectDir = tempdir()
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .withName(PROJECT_NAME)
            .build()

        afterSpec {
            DockerService.getInstance(project.gradle).close()
        }

        with(project) {
            pluginManager.apply(DockerPlugin::class.java)
            version = PROJECT_VERSION
        }

        describe("with task of type BuildImageTask") {
            project.tasks {
                val imagename by registering(BuildImageTask::class)

                it("should be able to create BuildImageTasks") {
                    imagename.shouldNotBeNull()
                    imagename.name shouldBe "imagename"
                }
                it("should have a default tags") {
                    val tags = imagename.get().tags.get()

                    tags.shouldContainAll(
                        "$PROJECT_NAME/imagename:$PROJECT_VERSION",
                        "$PROJECT_NAME/imagename:latest"
                    )
                }
                it("should have default output file") {
                    imagename.get().imageId - projectDir shouldBe
                            "build/.docker/${project.name}/${imagename.get().name}"
                }
                it("should have default input directory") {
                    imagename.get().inputDir - projectDir shouldBe "docker/imagename"
                }
                it("should be possible to override default tags") {

                    imagename {
                        tags.add("hello world")
                    }

                    imagename.get().tags.get().shouldContainAll(
                        "hello world"
                    )
                }
                describe("Dockerfile with arguments") {
                    val fixture = Fixtures("docker/withargs").clone(tempdir())
                    val imagewithargs by registering(BuildImageTask::class) {
                        inputDir.set(fixture)
                    }

                    afterTest { imagewithargs.forceRemoveImage() }

                    it("should be possible to set args") {
                        imagewithargs {
                            args.put("BASE", "scratch")
                        }

                        imagewithargs.get().execute()

                        DockerService.getInstance(project.gradle)
                            .listImages()
                            .map { it.shortId }
                            .shouldContain(imagewithargs.get().imageId.get().asFile.readText())
                    }
                    it("should fail with no args") {
                        val exception = shouldThrowExactly<DockerClientException> {
                            imagewithargs.get().execute()
                        }

                        exception.message shouldBe "Could not build image: base name (\${BASE}) should not be blank"
                    }
                }
            }
        }
    }
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    companion object {
        const val PROJECT_NAME = "test"
        const val PROJECT_VERSION = "1.2.3"
    }
}

