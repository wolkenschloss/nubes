package wolkenschloss.gradle.docker

import com.github.dockerjava.api.model.Mount
import com.github.dockerjava.api.model.MountType
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContain

import org.gradle.testfixtures.ProjectBuilder

class MountsTest : FunSpec({
    context("A ContainerMounts property") {

        val projectDir = tempdir()
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()

        val mounts = project.objects.newInstance(ContainerMounts::class.java)

        test("should add input file mounts") {
            val containerTargetFile = "/mnt/opt/app/datafile"
            val projectSourceFile = project.layout.projectDirectory.file("datafile")

            mounts.input {
                file {
                    target.convention(containerTargetFile)
                    source.convention(projectSourceFile)
                }
            }

            mounts.mounts.get() shouldContain Mount()
                .withSource(projectSourceFile.asFile.absolutePath)
                .withTarget(containerTargetFile)
                .withType(MountType.BIND)
                .withReadOnly(true)
        }

        test("should add input directory mount") {
            val containerTargetDirectory = "/mnt/opt/app/dataDir"
            val projectSourceDirectory = project.layout.projectDirectory.dir("dataDir")

            mounts.input {
                directory {
                    target.convention(containerTargetDirectory)
                    source.convention(projectSourceDirectory)
                }
            }

            mounts.mounts.get() shouldContain Mount()
                .withSource(projectSourceDirectory.asFile.absolutePath)
                .withTarget(containerTargetDirectory)
                .withType(MountType.BIND)
                .withReadOnly(true)
        }

        test("should add output directory mount") {
            val containerTargetDirectory = "/mnt/opt/app/dataDir"
            val projectBuildOutput = project.layout.buildDirectory.dir("data")

            mounts.output {
                target.convention(containerTargetDirectory)
                source.convention(projectBuildOutput)
            }

            mounts.mounts.get() shouldContain Mount()
                .withSource(projectBuildOutput.get().asFile.absolutePath)
                .withTarget(containerTargetDirectory)
                .withType(MountType.BIND)
                .withReadOnly(false)
        }
    }
})