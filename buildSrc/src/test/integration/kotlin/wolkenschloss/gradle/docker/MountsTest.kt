//package wolkenschloss.gradle.docker
//
//import io.kotest.core.spec.style.FunSpec
//import io.kotest.engine.spec.tempdir
//import org.gradle.testfixtures.ProjectBuilder
//
//class MountsTest : FunSpec({
//    context("A ContainerMounts property") {
//
//        val projectDir = tempdir()
//        val project = ProjectBuilder.builder()
//            .withProjectDir(projectDir)
//            .build()
//
//        val mounts = project.objects.property(ContainerMounts::class.java)
//
//        test("should add input file mounts") {
//            mounts.get().input {
//
//            }
//        }
//    }
//})