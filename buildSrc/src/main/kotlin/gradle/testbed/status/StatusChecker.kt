package family.haschka.wolkenschloss.gradle.testbed.status

internal interface StatusChecker {
    fun run(label: String)
}