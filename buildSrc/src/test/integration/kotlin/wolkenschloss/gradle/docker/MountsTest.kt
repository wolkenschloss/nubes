package wolkenschloss.gradle.docker

import com.github.dockerjava.api.model.Mount
import com.github.dockerjava.api.model.MountType
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContain

import io.kotest.matchers.shouldNotBe
import org.gradle.testfixtures.ProjectBuilder

class MountsTest : FunSpec({
    context("A ContainerMounts property") {

        val projectDir = tempdir()
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()

        val mounts = project.objects.newInstance(ContainerMounts::class.java)

        test("should not be null") {
            withClue("mounts should not be null") {
                mounts shouldNotBe null
            }
        }

        test("should have object factory") {
            withClue("getObjectFactory should return not null") {
                mounts.objectFactory shouldNotBe null
            }
        }

        test("should add input file mounts") {
            val t = "/mnt/opt/app/datafile"
            val s = project.layout.projectDirectory.file("datafile")

            mounts.input {
                target.convention(t)
                      source.convention(s)
                }

            mounts.mounts.get() shouldContain             Mount()
                .withSource(s.asFile.absolutePath)
                .withTarget(t)
                .withType(MountType.BIND)
                .withReadOnly(true)
        }

        test("should add output directory mount") {
            val s = "/mnt/opt/app/datadir"
            val dir = project.layout.buildDirectory.dir("data")

            mounts.output {
                target.convention(s)
                source.convention(dir)
            }

            mounts.mounts.get() shouldContain Mount()
                .withSource(dir.get().asFile.absolutePath)
                .withTarget(s)
                .withType(MountType.BIND)
                .withReadOnly(false)
        }
    }
})