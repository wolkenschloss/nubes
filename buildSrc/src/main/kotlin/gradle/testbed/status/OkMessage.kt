package family.haschka.wolkenschloss.gradle.testbed.status

import java.util.function.Function

internal interface OkMessage<T> {
    fun ok(message: Function<T, String>): ErrorMessage<T>
}