package family.haschka.wolkenschloss.gradle.testbed.domain

import com.jayway.jsonpath.JsonPath
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.inject.Inject

abstract class Launch : DefaultTask() {

    @get:Input
    abstract val domain: Property<String>

    @get:InputFile
    abstract val userData: Property<File>

    @get:Input
    abstract val hosts: ListProperty<String>

    @get:Input
    abstract val domainSuffix: Property<String>

    @get:Input
    abstract val disk: Property<String>

    @get:Input
    abstract val mem: Property<String>

    @get:Input
    abstract val cpus: Property<Int>

    @get:Input
    abstract val image: Property<String>

    @get:OutputFile
    abstract val hostsFile: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    data class Result(val output: String, val exitCode: Int)

    private fun execute(project: Project, action: ExecSpec.() -> Unit): Result {
        ByteArrayOutputStream().use {
            val result = project.exec {
                standardOutput = it
                action(this)
            }
            return Result(it.toString(), result.exitValue)
        }
    }

    private fun execute(project: Project, input: Property<File>, action: ExecSpec.() -> Unit): Result {
        input.get().inputStream().use { inputStream ->
            ByteArrayOutputStream().use {
                val result = project.exec {
                    standardOutput = it
                    standardInput = inputStream
                    action(this)
                }
                return Result(it.toString(), result.exitValue)
            }
        }
    }

    @TaskAction
    fun exec() {
        launch()
        val ip = DomainOperations(execOperations, domain).ipAddress()
        updateHosts(ip)

        logger.lifecycle("Testbed IP Address: {}", ip)
    }

    private fun launch() {

        logger.info("Prüfe, ob Instanz existiert")

        val r1 = execute(project) {
            commandLine("multipass", "info", "--format", "json", domain.get())
            isIgnoreExitValue = true
        }

        if (r1.exitCode == 0) {
            val path = "\$.info.${domain.get()}.state"
            val state: String = JsonPath.parse(r1.output).read(path)

            logger.info("Testbed instance is in state {}", state)

            when(state) {
                "Running" -> return
                "Suspended", "Stopped" -> start()
                else -> throw UnknownState(domain.get(), state)
            }
        }

        logger.quiet("Instanz wird erzeugt")

        val commands = listOf("multipass", "launch",
            "--name", domain.get(),
            "--timeout", "900")
            .asSequence()
            .plus(cpus.map { listOf("--cpus", it.toString()) }.getOrElse(emptyList()))
            .plus(disk.map { listOf("--disk", it) }.getOrElse(emptyList()))
            .plus(mem.map { listOf("--mem", it) }.getOrElse(emptyList()))
            .plus(listOf("--cloud-init", "-"))
            .plus(image.map { listOf(it) }.getOrElse(emptyList()))
            .toList()

        // Erzeuge:
        execute(project, userData) {
            commandLine = commands
        }
    }

    private fun start() {
        // Stopped or Suspended
        logger.info("Die Instanz wird gestartet")
        execute(project) {
            commandLine("multipass", "start", "--verbose", domain.get())
        }
    }

    private fun updateHosts(ip: String) {
        logger.info("create hosts file")


        val path = hostsFile.get().asFile

        if (path.exists()) {
            path.delete()
        }

        logger.info("Writing hosts file: {}", path.absolutePath)

        val hosts = Stream.concat(
                Stream.of(ip),
                Stream.concat(
                        Stream.of(domain.get()),
                        hosts.get().stream()
                )
                        .map { host: String? -> String.format("%s.%s", host, domainSuffix.get()) })
                .collect(Collectors.joining(" ")) + "\n"

        path.writeText(hosts)
    }
}