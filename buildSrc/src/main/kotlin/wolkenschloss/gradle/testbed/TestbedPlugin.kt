package wolkenschloss.gradle.testbed

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class TestbedPlugin : Plugin<Project> {
    override fun apply(project: Project) {
//        val extension = project.objects.newInstance(TestbedExtension::class.java)
//        project.extensions.add(TestbedExtension::class.java, TESTBED_EXTENSION_NAME, extension)
        val extension = project.extensions
            .create(TESTBED_EXTENSION_NAME, TestbedExtension::class)
            .configure(project)
        val registrar = Registrar(project, extension)
        registrar.register()
    }

    companion object {
        const val TESTBED_EXTENSION_NAME = "testbed"
    }
}