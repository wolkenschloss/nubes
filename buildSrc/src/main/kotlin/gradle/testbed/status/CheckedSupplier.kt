package family.haschka.wolkenschloss.gradle.testbed.status

internal fun interface CheckedSupplier<T> {
    @Throws(Throwable::class)
    fun apply(): T
}