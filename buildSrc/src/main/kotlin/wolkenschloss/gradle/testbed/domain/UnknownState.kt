package wolkenschloss.gradle.testbed.domain

data class UnknownState(val instance: String, val state: String) : Throwable() {

    override val message: String?
        get() = "Unknown state '$state' of multipass instance '$instance'"
}
