package wolkenschloss.gradle.testbed

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import wolkenschloss.gradle.testbed.domain.DomainTasks
import wolkenschloss.gradle.testbed.download.DownloadTasks
import wolkenschloss.gradle.testbed.pool.BuildPool
import wolkenschloss.gradle.testbed.pool.PoolTasks
import wolkenschloss.gradle.testbed.status.StatusTasks
import wolkenschloss.gradle.testbed.transformation.TransformationTasks

class TestbedPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions
            .create(TESTBED_EXTENSION_NAME, TestbedExtension::class)
            .configure(project)

        project.registerTasks()
    }

    private fun Project.registerTasks() {
        val testbed = the(TestbedExtension::class)
        val values = testbed.asPropertyMap(this)

        TransformationTasks(values, testbed.transformation).register(tasks)
        DownloadTasks(testbed.baseImage).register(tasks)
        PoolTasks(testbed.pool).register(tasks)
        DomainTasks(testbed.domain, testbed.host.callbackPort).register(tasks)
        StatusTasks(testbed.domain, testbed.pool).register(tasks)
        registerDestroyTask(testbed)
    }

    private fun Project.registerDestroyTask(testbed: TestbedExtension) {
        val buildPool by tasks.existing(BuildPool::class)

        tasks.register(DESTROY_TASK_NAME, Destroy::class.java) {
            description = "Destroy testbed and delete all files."
            domain.convention(testbed.domain.name)
            poolRunFile.convention(buildPool.flatMap(BuildPool::poolRunFile))
            buildDir.convention(project.layout.buildDirectory)
        }
    }

    companion object {
        const val TESTBED_EXTENSION_NAME = "testbed"
        const val DESTROY_TASK_NAME = "destroy"
    }
}