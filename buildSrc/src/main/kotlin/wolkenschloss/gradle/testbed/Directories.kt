package wolkenschloss.gradle.testbed

import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object Directories {
    private const val APPLICATION_NAME = "wolkenschloss"
    private const val TESTBED_NAME = "testbed"
    private const val CERTIFICATE_AUTHORITY_NAME = "ca"

    val applicationHome: Path
        get() = xdgDataHome.resolve(APPLICATION_NAME)

    val testbedHome: Path
        get() = applicationHome.resolve(TESTBED_NAME)

    val certificateAuthorityHome: Path
        get() = applicationHome.resolve(CERTIFICATE_AUTHORITY_NAME)

    val xdgDataHome: Path
        get() = Optional.ofNullable(System.getenv("XDG_DATA_HOME"))
            .map { first: String? -> Paths.get(first) }
            .orElse(Paths.get(System.getProperty("user.home"), ".local", "share"))
}
