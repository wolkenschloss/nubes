package family.haschka.wolkenschloss.gradle.testbed.status

import org.gradle.api.GradleException
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

internal class StatusBuilder<T>(statusTask: Status, fn: () -> T) :
    Check<T>, OkMessage<T>, ErrorMessage<T>, StatusChecker {
    private val statusTask: Status
    private val fn: () -> T
    private var predicate: Optional<Predicate<T>> = Optional.empty()
    private var errorMessage: Optional<String> = Optional.empty()
    private var okMessageProducer: Optional<Function<T, String>> = Optional.empty()

    init {
        this.statusTask = statusTask
        this.fn = fn
    }

    override fun check(p: Predicate<T>): OkMessage<T> {
        predicate = Optional.of(p)
        return this
    }

    override fun ok(message: Function<T, String>): ErrorMessage<T> {
        okMessageProducer = Optional.of(message)
        return this
    }


    override fun error(message: String): StatusChecker {
        errorMessage = Optional.of(message)
        return this
    }

    override fun run(label: String) {
        val value: T  = try {
            fn()
        } catch (throwable: Throwable) {
            statusTask.logger.error(String.format("✗ %-15s: %s", label, throwable.message))
            return
        }

        predicate.filter { it.test(value) }.ifPresentOrElse({
            okMessageProducer.map { it.apply(value) }
                .map { String.format("✓ %-15s: %s", label, it) }
                .ifPresentOrElse( {
                    statusTask.logger.quiet(it)
                },
                    {
                        errorMessage.ifPresent {
                            statusTask.logger.error(String.format("✗ %-15s: %s", label, it))
                        }
                    })}, {
                        throw GradleException("No test for $label")
        })
    }
}