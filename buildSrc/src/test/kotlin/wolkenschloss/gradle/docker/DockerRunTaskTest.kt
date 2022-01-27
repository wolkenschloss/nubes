package wolkenschloss.gradle.docker

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.kotlin.dsl.*

class DockerRunTaskTest : DescribeSpec ({
    describe("A project with DockerPlugin applied") {
        val projectDir = tempdir()
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()

        afterSpec {
            DockerService.getInstance(project.gradle).close()
        }

        project.pluginManager.apply(DockerPlugin::class.java)

        describe("with task of type DockerRunTask") {
            project.tasks {
                val hello by registering(DockerRunTask::class)


                it("should fail when no image is configured") {
                    hello.get().execute()
                }
            }
        }
    }
}){
}