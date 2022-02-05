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
            notBefore.convention(today)
            notAfter.convention(expireIn())
            certificate.convention(target.layout.buildDirectory.file("ca/ca.crt"))
            privateKey.convention(target.layout.buildDirectory.file("ca/ca.key"))
        }
    }

    private fun expireIn(): ZonedDateTime {
        return today.plus(CreateTask.DEFAULT_VALIDITY_PERIOD)
    }

    private val today = ZonedDateTime.now(ZoneOffset.UTC)

    companion object {
        const val NAME = "wolkenschloss.gradle.ca"
    }
}
