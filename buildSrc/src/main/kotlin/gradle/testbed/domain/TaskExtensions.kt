package family.haschka.wolkenschloss.gradle.testbed.domain

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.process.ExecSpec
import java.io.ByteArrayOutputStream

data class Result(val task: DefaultTask, val output: String, val error: String, val exitCode: Int) {
    fun verify(): Result {
        if (exitCode != 0) {
            task.logger.error(output)
            task.logger.error(error)
            throw GradleException("Task ${task.name} exists with error code $exitCode")
        }
        return this
    }
}

fun DefaultTask.execute(action: ExecSpec.() -> Unit): Result {
    ByteArrayOutputStream().use { stdout ->
        ByteArrayOutputStream().use { stderr ->
            val result = project.exec {
                errorOutput = stderr
                standardOutput = stdout
                isIgnoreExitValue = true
                action(this)
            }

            return Result(this, stdout.toString(), stderr.toString(), result.exitValue)
        }
    }
}
