package wolkenschloss.gradle.testbed.pool

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
// TODO: Nicht in der Hierarchie nach oben greifen
import wolkenschloss.gradle.testbed.TestbedExtension
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.file.Files
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Optional
import java.util.function.Function
import javax.inject.Inject

/**
 * Erzeugt das Festplatten-Abbild für die virtuelle Maschine
 * des Prüfstandes.
 */
// Wenn es mal wieder Probleme mit Permission Denied gibt, wenn die
// VM startet soll: Einfach mal appamor für diese VM ausschalten:
@CacheableTask
abstract class BuildRootImage : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val baseImage: RegularFileProperty

    @get:Input
    abstract val size: Property<String>

    @get:Internal
    abstract val rootImage: RegularFileProperty

    @get:OutputFile
    abstract val rootImageMd5File: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    @Throws(NoSuchAlgorithmException::class, IOException::class)
    fun exec() {
        val created = rootImage.get().asFile.parentFile.mkdirs()
        val extension: Optional<TestbedExtension> = Optional.ofNullable<TestbedExtension>(
            project.extensions.findByType<TestbedExtension>(
                TestbedExtension::class.java
            )
        )
        val failOnError =
            extension.map { ext: TestbedExtension -> ext.failOnError.get() }
                .orElse(true)
        logger.info("Testbed extension found: {}", extension.isPresent())
        logger.info("failOnError set to {}", failOnError)
        if (created) {
            logger.info("Directory created")
        }
        exec {
                commandLine("qemu-img")
                .args(
                    "create", "-f", "qcow2", "-F", "qcow2", "-b",
                    baseImage.get(),
                    rootImage.get()
                ).isIgnoreExitValue = !failOnError
        }

        exec {
            commandLine("qemu-img")
                .args("resize", rootImage.get(), size.get()).isIgnoreExitValue = !failOnError
        }

        logger.info("Root image created")
        val hash = baseImage.get().asFile.absolutePath + size.get()
        val md = MessageDigest.getInstance("MD5")
        md.update(hash.toByteArray())
        Files.write(rootImageMd5File.asFile.get().toPath(), md.digest())
        logger.info(String.format("Root image MD5 hash is %032x", BigInteger(1, md.digest())))
    }

    @Throws(IOException::class)
    private fun exec(spec: Action<in ExecSpec>) {
        ByteArrayOutputStream().use { stdout ->
            val result = execOperations.exec {
                logger.info("Executing {}", java.lang.String.join(" ", this.commandLine))
                spec.execute(this)
                this.standardOutput = stdout
            }
            logger.info(stdout.toString())
            logger.info("Execute result {}", result.exitValue)
        }
    }
}