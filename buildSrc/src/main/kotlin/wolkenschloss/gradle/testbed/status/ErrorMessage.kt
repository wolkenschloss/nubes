package wolkenschloss.gradle.testbed.status


internal interface ErrorMessage<T> {
    fun error(message: String): StatusChecker
}