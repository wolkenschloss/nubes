package wolkenschloss.gradle.testbed.domain

import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.process.ExecOperations
import wolkenschloss.gradle.testbed.domain.SecureShellService.ShellCommand
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.function.Consumer

class SecureShellService(
    private val execOperations: ExecOperations,
    private val ip: String,
    private val knownHostsFile: Provider<RegularFile>
) {
    class Result(val stdout: String, val stderr: String, val exitValue: Int)

    fun interface ShellCommand<T> {
        fun execute(fn: T)
    }

    fun command(vararg args: String): ShellCommand<Consumer<Result>> {
        return ShellCommand { consumer: Consumer<Result> ->
            val arguments = listOf(
                    "-o", String.format("UserKnownHostsFile=%s", knownHostsFile.get().asFile.path),
                    ip)

            try {
                ByteArrayOutputStream().use { stdout ->
                    ByteArrayOutputStream().use { stderr ->
                        val result = execOperations.exec{
                            commandLine("ssh")
                                .args(arguments + args)
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

    fun withCommand(args: List<String>, method: (Result) -> Unit) {
        command(*args.toTypedArray()).execute(method)
    }
}