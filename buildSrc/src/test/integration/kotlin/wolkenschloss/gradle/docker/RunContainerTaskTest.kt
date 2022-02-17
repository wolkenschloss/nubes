package wolkenschloss.gradle.docker

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.testfixtures.ProjectBuilder

class RunContainerTaskTest : DescribeSpec ({
    describe("A project with DockerPlugin applied") {
        val projectDir = tempdir()
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()

        afterSpec {
            DockerService.getInstance(project.gradle).close()
        }

        project.pluginManager.apply(DockerPlugin::class.java)

        describe("with task of type RunTask") {

            project.tasks {
                val hello by registering(RunContainerTask::class)

                it("should fail when no image is configured") {
                    hello.get().execute()
                }

                it("should execute shell command") {
                    hello {
                        command.addAll("echo", "hello world")
                    }

                    shouldNotThrowAny {
                        hello.get().execute()
                    }
                }

                it("should accept mount volumes") {
                    hello {
                        mount {
                            input {
                                file {
                                    source.set(project.layout.projectDirectory.file("hello"))
                                    target.set("/mnt/hello")
                                }
                                directory {
                                    source.set(project.layout.projectDirectory.dir("data"))
                                    target.set("/mnt/data")
                                }
                            }

                            output {
                                source.set(project.layout.buildDirectory.dir("out"))
                                target.set("/mnt/out")
                            }
                        }
                    }

                    hello.get().mount.mounts.get().count() shouldBe 3
                }
            }
        }
    }
})

