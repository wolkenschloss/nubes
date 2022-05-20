package family.haschka.wolkenschloss.gradle.testbed.status

import java.util.function.Predicate

internal interface Check<T> {
    fun check(p: Predicate<T>): OkMessage<T>
}