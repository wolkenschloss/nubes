package wolkenschloss.gradle.testbed

import org.gradle.api.GradleScriptException
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.annotation.Nonnull

abstract class UserExtension {
    fun initialize() {
        sshKeyFile.convention { Path.of(System.getenv("HOME"), ".ssh", "id_rsa.pub").toFile() }
        sshKey.convention(sshKeyFile.map { sshKeyFile: RegularFile -> readSshKey(sshKeyFile) })
        privateSshKeyFile.convention { Path.of(System.getenv("HOME"), ".ssh", "id_rsa").toFile() }
        name.convention(System.getenv("USER"))
    }

    abstract val sshKeyFile: RegularFileProperty
    abstract val sshKey: Property<String>
    abstract val privateSshKeyFile: RegularFileProperty
    abstract val name: Property<String>

    companion object {
        @Nonnull
        fun readSshKey(sshKeyFile: RegularFile): String {
            val file = sshKeyFile.asFile
            return try {
                Files.readString(file.toPath()).trim { it <= ' ' }
            } catch (e: IOException) {
                throw GradleScriptException("Can not read public ssh key", e)
            }
        }
    }
}