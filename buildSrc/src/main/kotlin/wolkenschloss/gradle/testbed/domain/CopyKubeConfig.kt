package wolkenschloss.gradle.testbed.domain

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import javax.inject.Inject

abstract class CopyKubeConfig : DefaultTask() {
    @get:Input
    abstract val domain: Property<String>

    @get:InputFile
    abstract val knownHostsFile: RegularFileProperty

    @get:OutputFile
    abstract val kubeConfigFile: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    @Throws(Throwable::class)
    fun read() {
        val domainOperations = DomainOperations.getInstance(project.gradle).get()

        val shell: SecureShellService = domainOperations.getShell(domain, knownHostsFile, execOperations)
        shell.command("microk8s", "config").execute {result ->
            val permissions = setOf(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ)
            val attributes = PosixFilePermissions.asFileAttribute(permissions)
            val path = kubeConfigFile.get().asFile.toPath()
            val kubeconfig: String = result.stdout
            if (Files.exists(path)) {
                try {
                    if (Files.readString(path).compareTo(kubeconfig) == 0) {
                        logger.info("kubernetes configuration file already exists")
                        throw StopActionException()
                    }
                } catch (e: IOException) {
                    throw GradleException("Can not read file")
                }
            }

            val file: Path = try {
                Files.createFile(path, attributes)
            } catch (exception: IOException) {
                throw GradleException("Can not create File", exception)
            }

            try {
                Files.writeString(
                    file.toAbsolutePath(),
                    result.stdout,
                    StandardOpenOption.WRITE
                )
            } catch (exception: IOException) {
                throw GradleException("Can not write File", exception)
            }
        }
    }
}