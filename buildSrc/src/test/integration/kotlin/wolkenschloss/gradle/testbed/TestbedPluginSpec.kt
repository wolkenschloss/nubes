package family.haschka.wolkenschloss.gradle.testbed

import io.kotest.core.script.describe
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder
import family.haschka.wolkenschloss.gradle.ca.CaPlugin

class TestbedPluginSpec : FunSpec({
   describe("A project with testbed plugin applied to") {
      val projectDir = tempdir()
      val project = ProjectBuilder.builder()
         .withProjectDir(projectDir)
         .withName("testbed")
         .build()

      project.pluginManager.apply(TestbedPlugin::class.java)

      test("should also have ca plugin applied") {
         project.plugins.findPlugin(CaPlugin::class.java).shouldNotBeNull()
      }
   }
})