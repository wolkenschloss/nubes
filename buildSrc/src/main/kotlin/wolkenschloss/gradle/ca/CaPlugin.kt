package wolkenschloss.gradle.ca

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZoneOffset
import java.time.ZonedDateTime

class CaPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks.withType(CreateTask::class.java).configureEach {
            val projectData = project.providers.xdgDataHome.resolve("wolkenschloss/ca")
            println("Configure CreateTask")
            notBefore.convention(today)
            notAfter.convention(expireIn())
            certificate.convention(projectData.resolve("ca.crt"))
            privateKey.convention(projectData.resolve("ca.key"))
            println("Configuration finished")
        }
    }

    private val ProviderFactory.xdgDataHome: Provider<Path>
        get() {
            return environmentVariable("XDG_DATA_HOME")
                .zip(systemProperty("user.home")) { left, right ->
                    if (left.isNullOrEmpty()) {
                        Paths.get(right, ".local", "share")
                    } else {
                        Paths.get(left)
                    }
                }
        }

    private fun Provider<Path>.resolve(path: String): Provider<Path> {
        return this.map { it.resolve(path) }
    }

    private val Provider<Path>.asFile: Provider<File>
        get() = this.map(Path::toFile)

    private fun expireIn(): ZonedDateTime {
        return today.plus(CreateTask.DEFAULT_VALIDITY_PERIOD)
    }

    private val today = ZonedDateTime.now(ZoneOffset.UTC)

    companion object {
        const val NAME = "wolkenschloss.gradle.ca"
    }
}
