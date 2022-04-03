package wolkenschloss.gradle.testbed

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*
import wolkenschloss.gradle.testbed.domain.DomainTasks
import wolkenschloss.gradle.testbed.domain.PushImage
import wolkenschloss.gradle.testbed.status.StatusTasks
import wolkenschloss.gradle.testbed.transformation.TransformationTasks
import wolkenschloss.gradle.ca.CaPlugin
import wolkenschloss.gradle.ca.CreateTask
import wolkenschloss.gradle.ca.TruststoreTask

class TestbedPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(CaPlugin::class.java)

        val ca by project.tasks.existing(CreateTask::class)
        val truststore by project.tasks.existing(TruststoreTask::class)

        val testbed = project.extensions
            .create(TESTBED_EXTENSION_NAME, TestbedExtension::class)
            .configure(ca)

        project.registerTasks(ca, truststore)

        project.tasks.withType(PushImage::class) {
            registry.convention(testbed.registry)
            this.truststore.convention(truststore.flatMap { it.truststore })
        }
    }

    private fun Project.registerTasks(ca: TaskProvider<CreateTask>, truststore: TaskProvider<TruststoreTask>) {
        val testbed = the(TestbedExtension::class)
        val values = testbed.asPropertyMap(this)

        TransformationTasks(values, testbed.transformation, ca).register(tasks)

        DomainTasks(testbed.domain).register(tasks)
        StatusTasks(
            testbed.domain,
            testbed.registry,
            ca.flatMap { it.certificate },
            truststore.flatMap { it.truststore })
            .register(tasks)

        registerDestroyTask(testbed)
    }

    private fun Project.registerDestroyTask(testbed: TestbedExtension) {


        tasks.register(DESTROY_TASK_NAME, Destroy::class.java) {
            description = "Destroy testbed and delete all files."
            domain.convention(testbed.domain.name)
            buildDir.convention(project.layout.buildDirectory)
        }
    }

    companion object {
        const val TESTBED_EXTENSION_NAME = "testbed"
        const val DESTROY_TASK_NAME = "destroy"
    }
}