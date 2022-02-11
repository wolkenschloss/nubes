package wolkenschloss.gradle.testbed.download

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.Path
import java.util.*
import javax.inject.Inject

@DisableCachingByDefault
abstract class DownloadDistribution : DefaultTask() {
    @get:Inject
    protected abstract val fileSystemOperations: FileSystemOperations

    @get:Inject
    protected abstract val execOperations: ExecOperations

    @get:Input
    abstract val baseImageLocation: Property<String>

    @get:Input
    abstract val distributionName: Property<String>

    @get:Internal
    abstract val baseImage: RegularFileProperty

    //    @Internal
    @get:OutputDirectory
    abstract val distributionDir: DirectoryProperty

    @get:Throws(MalformedURLException::class)
    private val baseImageUrl: URL
        get() = URL(baseImageLocation.get())

    @get:Throws(MalformedURLException::class, URISyntaxException::class)
    private val sha256SumsUrl: URL
        get() {
            val location = baseImageUrl.toURI()
            val parent = if (location.path.endsWith("/")) location.resolve("..") else location.resolve(".")
            return parent.resolve("SHA256SUMS").toURL()
        }

    @get:Throws(MalformedURLException::class, URISyntaxException::class)
    private val gpgFileUrl: URL
        private get() {
            val location = baseImageUrl.toURI()
            val parent = if (location.path.endsWith("/")) location.resolve("..") else location.resolve(".")
            return parent.resolve("SHA256SUMS.gpg").toURL()
        }

    @TaskAction
    @Throws(URISyntaxException::class, IOException::class)
    fun download() {
        downloadFile(baseImageUrl)
        downloadFile(sha256SumsUrl)
        downloadFile(gpgFileUrl)
        verifySignature()
        verifyChecksum()
        changePermissions()
    }

    private fun changePermissions() {
        val file = File(baseImage.asFile.get().toPath().toString())
        val dir = file.parentFile
        Arrays.stream(dir.listFiles()).forEach { f: File ->
            val success = (f.setWritable(false, false)
                    && f.setReadable(true, true)
                    && f.setExecutable(false, false))
            if (!success) {
                val message = String.format("Can not change file permissions: %s", file.path)
                logger.warn(message)
            }
        }
    }

    private fun downloadPath(filename: String): Provider<RegularFile> {
        return distributionDir.file(filename)
    }

    private fun verifyChecksum() {
        execOperations.exec {
            commandLine("sha256sum")
                .args(
                    "--ignore-missing",
                    "--check",
                    "SHA256SUMS"
                )
                .workingDir(distributionDir.get())
        }
            .assertNormalExitValue()
    }

    private fun verifySignature() {
        execOperations.exec {
            commandLine("gpg")
                .args(
                    "--keyid-format", "long", "--verify",
                    downloadPath("SHA256SUMS.gpg").get(),
                    downloadPath("SHA256SUMS").get()
                )
        }
            .assertNormalExitValue()
    }

    @Throws(IOException::class)
    private fun downloadFile(src: URL) {
        logger.info("Downloading file {}", src)
        val filename = Path.of(src.file).fileName
        val dst = distributionDir.file(filename.toString()).get().asFile
        dst.parentFile.mkdirs()
        if (dst.exists()) {
            return
        }
        src.openStream().use { input -> FileOutputStream(dst).use { output -> input.transferTo(output) } }
    }
}