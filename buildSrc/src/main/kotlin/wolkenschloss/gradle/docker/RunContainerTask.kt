package wolkenschloss.gradle.docker

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getValue

abstract class RunContainerTask : DefaultTask() {

    @get:Input
    abstract val imageId: Property<String>

    @get:Input
    abstract val command: ListProperty<String>

    @get:Internal
    abstract val dockerService: Property<DockerService>

    @TaskAction
    fun execute() {
//        logging.captureStandardOutput(LogLevel.QUIET)
        val docker by dockerService

        val hostConfig = HostConfig.newHostConfig()
            .withAutoRemove(true)

        val container = docker.client.createContainerCmd(imageId.get())
            .withCmd(*command.get().toTypedArray())
            .withHostConfig(hostConfig)
            .withAttachStderr(true)
            .withAttachStdout(true)
            .withTty(false)
            .exec()

        ConsoleLogger(logger).use { logger ->
            docker.client.attachContainerCmd(container.id)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .exec(logger)

            docker.client.startContainerCmd(container.id).exec()

            val status = docker.client.waitContainerCmd(container.id)
                .start().awaitStatusCode()

            if (status != 0){
                throw GradleException("Container exited with status code $status")
            }
        }
    }

    class ConsoleLogger(private val logger: Logger) : ResultCallback.Adapter<Frame>() {

        override fun onNext(item: Frame?) {
            if (item != null) {
                print(String(item.payload))
                logger.quiet(String(item.payload))
            }

            super.onNext(item)
        }
    }
}
