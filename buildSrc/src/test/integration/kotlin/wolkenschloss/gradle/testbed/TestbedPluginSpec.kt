package wolkenschloss.gradle.testbed

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import org.gradle.testfixtures.ProjectBuilder

class TestbedPluginSpec : FunSpec({
   context("Project with testbed plugin applied") {
       val projectDir = tempdir()
       val project = ProjectBuilder.builder()
           .withProjectDir(projectDir)
           .withName("testbed")
           .build()

       test("mach!") {
            project.pluginManager.apply(TestbedPlugin::class.java)
       }
   }
})