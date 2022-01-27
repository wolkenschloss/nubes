package wolkenschloss.gradle.docker

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.testfixtures.ProjectBuilder
import java.io.File


class BuildImageTaskTest : DescribeSpec({

    describe("A project with DockerPlugin applied") {
        val projectDir = tempdir()
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .withName(PROJECT_NAME)
            .build()

        with(project) {
            pluginManager.apply(DockerPlugin::class.java)
            version = PROJECT_VERSION
        }

        describe("with unconfigured build image task") {
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
            }
        }

        it("should be possible to override default tags") {
            project.tasks {
                val imagename by registering(BuildImageTask::class) {
                    tags.add("hello world")
                }
                imagename.get().tags.get().shouldContainAll(
                    "hello world"
                )
            }
        }
    }
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerTest

    companion object {
        const val PROJECT_NAME = "test"
        const val PROJECT_VERSION = "1.2.3"
    }
}

private operator fun <T : FileSystemLocation> FileSystemLocationProperty<T>.minus(projectDir: File): String {
    return get().asFile.relativeToOrNull(projectDir).toString()
}