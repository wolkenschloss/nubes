package wolkenschloss.gradle.testbed

import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.existing
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import wolkenschloss.gradle.testbed.domain.DomainTasks
import wolkenschloss.gradle.testbed.download.DownloadTasks
import wolkenschloss.gradle.testbed.pool.BuildPool
import wolkenschloss.gradle.testbed.pool.PoolTasks
import wolkenschloss.gradle.testbed.status.StatusTasks
import wolkenschloss.gradle.testbed.transformation.TransformationTasks

class Registrar(private val project: Project, private val extension: TestbedExtension) {
    fun register() {
        val tasks = project.tasks
        val values = extension.asPropertyMap(project)
        TransformationTasks(values, extension.transformation).register(tasks)
        DownloadTasks(extension.baseImage).register(tasks)
        PoolTasks(extension.pool).register(tasks)
        DomainTasks(extension.domain, extension.host.callbackPort).register(tasks)
        StatusTasks(extension.domain, extension.pool).register(tasks)
        registerDestroyTask(tasks)
    }

    private fun registerDestroyTask(tasks: TaskContainer) {
        val buildPool by tasks.existing(BuildPool::class)

        tasks.register(DESTROY_TASK_NAME, Destroy::class.java) {
            description = "Destroy testbed and delete all files."
            poolOperations.set(extension.pool.poolOperations)
            domain.convention(extension.domain.name)
            poolRunFile.convention(buildPool.flatMap(BuildPool::poolRunFile))
            buildDir.convention(project.layout.buildDirectory)
            domainOperations.set(extension.domain.domainOperations)
        }
    }

    companion object {
        const val DESTROY_TASK_NAME = "destroy"
    }
}