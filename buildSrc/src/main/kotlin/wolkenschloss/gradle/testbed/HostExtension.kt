package wolkenschloss.gradle.testbed

import org.gradle.api.provider.Property

abstract class HostExtension {
    fun initialize() {
        hostAddress.convention(IpUtil.hostAddress)
        callbackPort.set(DEFAULT_CALLBACK_PORT)
    }

    abstract val hostAddress: Property<String>
    abstract val callbackPort: Property<Int>

    companion object {
        const val DEFAULT_CALLBACK_PORT = 9191
    }
}