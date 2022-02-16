package wolkenschloss.gradle

import io.kotest.core.script.describe
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder
import wolkenschloss.gradle.ca.CaPlugin
import wolkenschloss.gradle.testbed.TestbedPlugin

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