package wolkenschloss.gradle.docker

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.getValue
import java.io.Closeable
import java.util.concurrent.TimeUnit

// A docker-java example showing how to start containers and read their output.
// https://github.com/docker-java/docker-java/blob/master/docker-java/src/test/java/com/github/dockerjava/cmd/AttachContainerCmdIT.java
abstract class RunContainerTask : DefaultTask() {

    @get:Optional
    @get:Input
    abstract val imageTag: Property<String>

    @get:Optional
    @get:InputFile
    abstract val imageId: RegularFileProperty

    @get:Nested
    abstract val mount: ContainerMounts

    fun mount(block: ContainerMounts.() -> Unit) {
        block(mount)
    }

    @get:Input
    abstract val command: ListProperty<String>

    @get:Optional
    @get:OutputFile
    abstract val logfile: RegularFileProperty

    @get:Internal
    abstract val dockerService: Property<DockerService>

    @get:Internal
    val image: Provider<String>
        get() = imageId
            .map { it.asFile.absoluteFile.readText() }
            .orElse(imageTag)

    @TaskAction
    fun execute() {

        logger.lifecycle("Executing task $name using docker image ${image.get()}")

        // this is equal to val docker = dockerService.get()
        val docker by dockerService

        val hostConfig = HostConfig.newHostConfig()
            .withAutoRemove(true)
            .withMounts(mount.mounts.get())

        logger.info("using image ${image.get()}")
        logger.info("create container")
        val container = docker.client.createContainerCmd(image.get())
            .withCmd(*command.get().toTypedArray())
            .withHostConfig(hostConfig)
            .withAttachStderr(true)
            .withAttachStdout(true)
            .withTty(false)
            .exec()

        val cb = ConsoleLogger(logger)

        logger.info("attach container ${container.id}")
        val attach = docker.client.attachContainerCmd(container.id)
            .withStdOut(true)
            .withStdErr(true)
            .withFollowStream(true)
            .exec(cb)

        // TODO: awaitStartedTimeout as optional property
        val started = attach.awaitStarted(10, TimeUnit.SECONDS)
        logger.lifecycle("Container attached: $started")

        logger.info("start container ${container.id}")
        docker.client.startContainerCmd(container.id).exec()

        logger.info("attach wait completion")
        // TODO: awaitCompletionTimeout as optional input property
        attach.awaitCompletion(10, TimeUnit.SECONDS)

        logger.info("wait container")
        val status = docker.client
            .waitContainerCmd(container.id)
            .start()
            .awaitStatusCode()

        if (status != 0) {
            throw GradleException("Container exited with status code $status")
        }

        if (logfile.isPresent) {
            val log = cb.log.toString()
            logger.info("Write ${log.length} characters to output file")
            logfile.get().asFile.writeText(log)
        }

        System.out.flush()
    }

    class ConsoleLogger(private val logger: Logger) : ResultCallback.Adapter<Frame>() {

        var log = StringBuilder()

        override fun onError(throwable: Throwable?) {
            logger.error("ConsoleLogger error:", throwable)
            super.onError(throwable)
        }

        override fun onComplete() {
            logger.lifecycle("ConsoleLogger on Complete!")
            super.onComplete()
        }

        override fun onStart(stream: Closeable?) {
            logger.lifecycle("ConsoleLogger onStart with Stream: ${stream != null}")
            super.onStart(stream)
        }

        override fun close() {
            logger.lifecycle("ConsoleLogger close")
            System.out.flush()
            super.close()
        }

        override fun onNext(item: Frame?) {


            if (item != null) {
                logger.quiet("ConsoleLogger onNext: ${item.streamType.name}")
                print(String(item.payload))
                System.out.flush()
                log.append(String(item.payload))
            }

            super.onNext(item)
        }
    }
}
