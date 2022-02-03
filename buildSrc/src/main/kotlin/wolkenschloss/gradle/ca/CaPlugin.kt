package wolkenschloss.gradle.ca

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.*

class CaPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.withType(CreateTask::class.java).configureEach {
            val calendar = Calendar.getInstance()
            calendar.time = today
            calendar.add(Calendar.YEAR, 5)
            notBefore.convention(today)
            notAfter.convention(calendar.time)
            certificate.convention(project.layout.buildDirectory.file("ca/ca.crt"))
            privateKey.convention(project.layout.buildDirectory.file("ca/ca.key"))
        }
    }

    val today = Date()

    companion object {
        const val NAME = "wolkenschloss.gradle.ca"
    }
}
