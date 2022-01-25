package wolkenschloss

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

abstract class DockerRunTask : DockerBaseTask() {

    class ConsoleLogger : ResultCallback.Adapter<Frame>() {

        override fun onNext(item: Frame?) {
            if (item != null) {
                print(String(item.payload))
            }

            super.onNext(item)
        }
    }

    @get:InputFile
    abstract val image: RegularFileProperty

    @get:Input
    abstract val cmd: ListProperty<String>

    @get:Nested
    abstract val configuration: Configuration

    @get:Internal
    abstract val containerLogLevel: Property<LogLevel>

    open fun volumes(action: Action<in Configuration>) {
        action.execute(this.configuration)
    }

    @TaskAction
    fun execute() {

        logging.captureStandardOutput(containerLogLevel.get())
        val mounts = configuration.mounts.get()

        val hostConfig = HostConfig.newHostConfig()
            .withMounts(mounts)
            .withAutoRemove(true)

        logger.debug("create container")

        val imageId = image.get().asFile.readText()
        val container = docker.createContainerCmd(imageId)
            .withHostConfig(hostConfig)
            .withCmd(*cmd.get().toTypedArray())
            .withAttachStdout(true)
            .withAttachStderr(true)
            .withTty(false)
            .exec()

        logger.debug("attach container")

        ConsoleLogger().use { callback ->

            docker.attachContainerCmd(container.id)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .exec(callback)

            logger.debug("start container")

            docker.startContainerCmd(container.id).exec()

            val status = docker.waitContainerCmd(container.id)
                .start()
                .awaitStatusCode()

            logger.debug("wait container: {}", status)

            if (status != 0) {
                throw GradleException("Container exited with status code $status")
            }
        }
    }
}