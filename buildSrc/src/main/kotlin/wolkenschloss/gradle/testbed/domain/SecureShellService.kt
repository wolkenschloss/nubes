package wolkenschloss.gradle.testbed.domain

import org.gradle.api.file.RegularFileProperty
import org.gradle.process.ExecOperations
import wolkenschloss.gradle.testbed.domain.SecureShellService.ShellCommand
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.function.Consumer

class SecureShellService(
    private val execOperations: ExecOperations,
    private val ip: String,
    private val knownHostsFile: RegularFileProperty
) {
    class Result(val stdout: String, val stderr: String, val exitValue: Int)

    fun interface ShellCommand<T> {
        fun execute(fn: T)
    }

    fun command(vararg args: Any?): ShellCommand<Consumer<Result>> {
        return ShellCommand { consumer: Consumer<Result> ->
            val arguments = Vector<Any?>()
            arguments.addAll(
                listOf(
                    "-o", String.format("UserKnownHostsFile=%s", knownHostsFile.get().asFile.path),
                    ip
                )
            )
            arguments.addAll(listOf(*args))
            try {
                ByteArrayOutputStream().use { stdout ->
                    ByteArrayOutputStream().use { stderr ->
                        val result = execOperations.exec{
                            commandLine("ssh")
                                .args(arguments)
                                .setStandardOutput(stdout).errorOutput = stderr
                        }.assertNormalExitValue()
                        consumer.accept(
                            Result(
                                stdout.toString().trim { it <= ' ' },
                                stderr.toString().trim { it <= ' ' },
                                result.exitValue
                            )
                        )
                    }
                }
            } catch (exception: IOException) {
                throw RuntimeException("Can not create streams.", exception)
            }
        }
    }

    fun withCommand(vararg args: Any): Consumer<Consumer<Result>> {
        return Consumer { consumer: Consumer<Result> -> command(*args).execute(consumer) }
    }
}